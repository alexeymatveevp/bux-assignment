package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.service.SubscriptionService;
import com.alexeymatveev.buxassignment.service.SingleProductTradingBot;
import com.alexeymatveev.buxassignment.util.ParseUtils;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Starts single trading bot instance in interactive mode.
 * User inputs:
 * - productID (optional)
 * - buy price (optional)
 * - lower limit (optional)
 * - upper limit (optional)
 *
 * Created by Alexey Matveev on 4/10/2018.
 */
public class SingleBotStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleBotStarter.class);

    private static String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

    private static List<String> productIds = Arrays.asList(AppConfig.getInstance().getString("sample.product.ids").split(","));

    public static void main(String[] args) throws Exception {
        // init some stuff
        LoggingConfig.init();
        BUXWebsocketClientEndpoint buxEndpoint = new BUXWebsocketClientEndpoint(new URI(webSocketUrl));
        SubscriptionService subscriptionService = new SubscriptionService(buxEndpoint);
        Scanner scanner = new Scanner(System.in);

        // stop main thread condition
        CountDownLatch stopProgramLatch = new CountDownLatch(1);

        // get productId or first
        System.out.print("ProductID (optional):");
        String productIdScanned = scanner.nextLine();
        if (productIdScanned == null || productIdScanned.equals("")) {
            productIdScanned = productIds.get(0);
        }
        final String productId = productIdScanned;
        // get the buy price or first current price
        System.out.print("Buy price (optional):");
        String buyPriceScanned = scanner.nextLine();
        // get lower boundary or -2 of current price
        System.out.print("Lower limit (optional):");
        String lowerLimitScanned = scanner.nextLine();
        // get upper boundary or +2 of current price
        System.out.print("Upper limit (optional):");
        String upperLimitScanned = scanner.nextLine();

        // set the limits
        Float buyPrice = ParseUtils.parseFloatOrDefault(buyPriceScanned, null);
        Float lowerLimit = ParseUtils.parseFloatOrDefault(lowerLimitScanned, null);
        Float upperLimit = ParseUtils.parseFloatOrDefault(upperLimitScanned, null);

        // init the bot
        SingleProductTradingBot bot = new SingleProductTradingBot(buxEndpoint, productId, buyPrice, lowerLimit, upperLimit);
        bot.addOnCompleteListener(stopProgramLatch::countDown);

        // subscribe and start bot on connect msg
        buxEndpoint.addOnConnectedListener(() -> {
            Result<Void> subscribe = subscriptionService.subscribe(productId);
            if (subscribe.isSuccessful()) {
                bot.start();
            }
        });

        // connect to web socket
        buxEndpoint.connect();

        // wait for bot to complete its work
        stopProgramLatch.await();

        // unsubscribe
        subscriptionService.unsubscribe(productId);

        // close the connection
        buxEndpoint.close();
    }
}
