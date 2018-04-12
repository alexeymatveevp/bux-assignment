package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.message.*;
import com.alexeymatveev.buxassignment.service.TradingBot;
import com.alexeymatveev.buxassignment.util.ParseUtils;
import com.alexeymatveev.buxassignment.util.SomeData;
import com.alexeymatveev.buxassignment.websocket.WebsocketClientEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Used to start single trading bot instance.
 * <p>
 * Created by Alexey Matveev on 4/10/2018.
 */
public class SingleBotStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleBotStarter.class);

    private static String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

    public static void main(String[] args) throws Exception {
        // init some stuff
        LoggingConfig.init();
        ObjectMapper objectMapper = new CommonObjectMapper();
        Scanner scanner = new Scanner(System.in);

        // stop main thread condition
        CountDownLatch stopProgramLatch = new CountDownLatch(1);

        // get productId or random
        System.out.print("ProductID (optional):");
        String productIdScanned = scanner.nextLine();
        if (productIdScanned == null || productIdScanned.equals("")) {
//                productIdScanned = SomeData.SOME_PRODUCT_IDS.get((int) (Math.random() * SomeData.SOME_PRODUCT_IDS.size()));
            productIdScanned = SomeData.SOME_PRODUCT_IDS.get(0);
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

        WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI(webSocketUrl));

        // init the bot
        TradingBot bot = new TradingBot(clientEndPoint, productId, buyPrice, lowerLimit, upperLimit);
        bot.addOnCompleteListener(stopProgramLatch::countDown);

        // wait for connect message and start the bot
        WebsocketClientEndpoint.OnMessageHandler connectHandler = new WebsocketClientEndpoint.OnMessageHandler() {
            @Override
            public void handleMessage(String message) {
                try {
                    BaseTMsg msg = objectMapper.readValue(message, BaseTMsg.class);
                    if (msg.getT() == MsgType.CONNECT_CONNECTED) {
                        // connected - start the bot
                        LOGGER.info("Web socket connection OK.");
                        clientEndPoint.removeMessageHandler(this);
                        bot.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        clientEndPoint.addMessageHandler(connectHandler);

        // connect to web socket
        clientEndPoint.connect();

        // wait for bot to complete its work
        stopProgramLatch.await();

        // close the connection
        clientEndPoint.close();
    }
}
