package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.message.BaseTMsg;
import com.alexeymatveev.buxassignment.model.message.MsgFactory;
import com.alexeymatveev.buxassignment.model.message.MsgType;
import com.alexeymatveev.buxassignment.model.message.SubscribeMsg;
import com.alexeymatveev.buxassignment.service.TradingBot;
import com.alexeymatveev.buxassignment.util.Mutable;
import com.alexeymatveev.buxassignment.util.SomeData;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class LimitedBalanceBotStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitedBalanceBotStarter.class);

    private static String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

    public static void main(String[] args) throws Exception {
        // init some stuff
        LoggingConfig.init();
        ObjectMapper objectMapper = new CommonObjectMapper();

        // init user wallet balance
        Mutable<Float> balance = new Mutable<>((float)(Math.random() * 5 + 30));

        // take 1 product to work with
        final String productId = SomeData.SOME_PRODUCT_IDS.get(0);
        System.out.println("Working with product: " + productId);

        // loop
        BUXWebsocketClientEndpoint buxEndpoint = new BUXWebsocketClientEndpoint(new URI(webSocketUrl));
        while (balance.getValue() > 0) {
            System.out.println("\n--\nCurrent balance: " + balance.getValue());
            // stop main thread condition
            CountDownLatch stopProgramLatch = new CountDownLatch(1);
            try {
                // init the bot (prices will be auto-detected)
                TradingBot bot = new TradingBot(buxEndpoint, productId, null, null, null);
                bot.addOnCompleteListener(() -> {
                    // unsubscribe
                    LOGGER.info("Unsubscribing from " + productId);
                    SubscribeMsg unsubscribeMsg = MsgFactory.createUnsubscribeMsg(productId);
                    String unsubscribeMsgJson = objectMapper.writeValueAsString(unsubscribeMsg);
                    buxEndpoint.sendMessage(unsubscribeMsgJson);
                    stopProgramLatch.countDown();
                });
                bot.addOnProfitAndLossListener((delta) -> balance.setValue(balance.getValue() + delta));

                // connect to web socket
                buxEndpoint.addOnConnectedListener(bot::start);
                buxEndpoint.connect();

                // start the bot


                // wait for not to complete
                stopProgramLatch.await();

            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        // close the connection
        buxEndpoint.close();
    }

}
