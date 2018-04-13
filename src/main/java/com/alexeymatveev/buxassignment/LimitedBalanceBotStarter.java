package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.service.SubscriptionService;
import com.alexeymatveev.buxassignment.service.SingleProductTradingBot;
import com.alexeymatveev.buxassignment.util.Mutable;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Starts user wallet balance simulation.
 * Program will run the trading bot while balance > 0.
 *
 * Created by Alexey Matveev on 4/12/2018.
 */
public class LimitedBalanceBotStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitedBalanceBotStarter.class);

    private static String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

    private static List<String> productIds = Arrays.asList(AppConfig.getInstance().getString("sample.product.ids").split(","));

    public static void main(String[] args) throws Exception {
        // init some stuff
        LoggingConfig.init();
        BUXWebsocketClientEndpoint buxEndpoint = new BUXWebsocketClientEndpoint(new URI(webSocketUrl));
        SubscriptionService subscriptionService = new SubscriptionService(buxEndpoint);

        // init user wallet balance
        Mutable<Float> balance = new Mutable<>((float)(Math.random() * 5 + 10));

        // take 1 product to work with
        final String productId = productIds.get(0);
        System.out.println("Working with product: " + productId);

        // loop
        while (balance.getValue() > 0) {
            System.out.println("\n--\nCurrent balance: " + String.format("%.2f", balance.getValue()) + "\n--");

            // stop main thread condition
            CountDownLatch stopProgramLatch = new CountDownLatch(1);

            // init the bot (prices will be auto-detected)
            SingleProductTradingBot bot = new SingleProductTradingBot(buxEndpoint, productId);

            // continue main thread after bot job is done
            bot.addOnCompleteListener(() -> {
                bot.stop();
                stopProgramLatch.countDown();
            });

            // change wallet balance
            bot.addOnProfitAndLossListener((delta) -> {
                if (delta > 0) {
                    System.out.println("WIN! (fee-less): " + delta);
                } else {
                    System.out.println("LOSS (fee-less): " + delta);
                }
                balance.setValue(balance.getValue() + delta);
            });

            if (!buxEndpoint.isConnected()) {
                // subscribe and start bot on connect msg
                buxEndpoint.addOnConnectedListener(() -> {
                    Result<Void> subscribe = subscriptionService.subscribe(productId);
                    if (subscribe.isSuccessful()) {
                        bot.start();
                    }
                });
                // connect to web socket
                buxEndpoint.connect();
            } else {
                // already connected - start bot
                bot.start();
            }

            // wait for not to complete
            stopProgramLatch.await();
        }

        // unsubscribe
        subscriptionService.unsubscribe(productId);

        // close the connection
        buxEndpoint.close();
    }

}
