package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.message.MsgFactory;
import com.alexeymatveev.buxassignment.model.message.SubscribeMsg;
import com.alexeymatveev.buxassignment.model.order.BuyOrderResponse;
import com.alexeymatveev.buxassignment.service.OrderService;
import com.alexeymatveev.buxassignment.util.JsonUtils;
import com.alexeymatveev.buxassignment.util.SomeData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LoggingConfig.init();

        CountDownLatch stopProgramLatch = new CountDownLatch(1);

        String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

        ObjectMapper objectMapper = new CommonObjectMapper();

        OrderService orderService = new OrderService();

        String productId = SomeData.SOME_PRODUCT_IDS.get(0);
        Result<BuyOrderResponse> buyOrderResult = orderService.buyOrder(productId, 200f, 1);
        if (buyOrderResult.isSuccessful()) {
            try {
                BuyOrderResponse buyOrderResponse = buyOrderResult.getData();
                // create 4 inputs
                String positionId = buyOrderResponse.getPositionId();
                float buyPrice = buyOrderResponse.getPrice().getAmount();
                float lowerLimit = buyPrice - 2;
                float upperLimit = buyPrice + 2;

                WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI(webSocketUrl));

                clientEndPoint.addMessageHandler((message) -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(message);
                        Optional<JsonNode> tOpt = JsonUtils.tryGetString(jsonNode, "t");
                        if (tOpt.isPresent()) {
                            JsonNode tNode = tOpt.get();
                            String tValue = tNode.textValue();
                            if (tValue.equals("connect.connected")) {
                                // subscription successful - ready to receive messages
                                LOGGER.info("Subscription OK.");
                                SubscribeMsg subscribeMsg = MsgFactory.createSubscribeMsg(productId);
                                String subscribeMsgJson = objectMapper.writeValueAsString(subscribeMsg);
                                clientEndPoint.sendMessage(subscribeMsgJson);
                            } else if (tValue.equals("trading.quote")) {
                                // price update
                                String priceText = jsonNode.get("body").get("currentPrice").textValue();
                                LOGGER.info("new price: " + priceText);
                                float newPrice = Float.parseFloat(priceText);
                                boolean closePosition = false;
                                if (newPrice < lowerLimit) {
                                    LOGGER.info("Price is lower then lower limit: " + newPrice + " < " + lowerLimit);
                                    closePosition = true;
                                } else if (newPrice > upperLimit) {
                                    LOGGER.info("Price is more then upper limit: " + newPrice + " > " + upperLimit);
                                    closePosition = true;
                                }
                                if (closePosition) {
                                    // ok we're done here!
                                    LOGGER.info("Selling order: "+ positionId);
                                    orderService.sellOrder(positionId);
                                    LOGGER.info("Unsubscribing from " + productId);
                                    SubscribeMsg unsubscribeMsg = MsgFactory.createUnsubscribeMsg(productId);
                                    String unsubscribeMsgJson = objectMapper.writeValueAsString(unsubscribeMsg);
                                    clientEndPoint.sendMessage(unsubscribeMsgJson);
                                    stopProgramLatch.countDown();
                                }
                            }
                        } else {
                            // skip for now
                            JsonNode body = jsonNode.get("body");
                            LOGGER.error("Failure: " + (body == null ? "null" : body.toString()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                clientEndPoint.connect();

                stopProgramLatch.await();

            } catch (URISyntaxException ex) {
                System.err.println("URISyntaxException exception: " + ex.getMessage());
            }
        } else {
            LOGGER.error("Cannot buy order: " + buyOrderResult.getErrorMsg());
        }
    }
}
