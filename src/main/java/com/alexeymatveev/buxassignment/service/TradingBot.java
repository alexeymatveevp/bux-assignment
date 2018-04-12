package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.message.*;
import com.alexeymatveev.buxassignment.model.order.BuyOrderResponse;
import com.alexeymatveev.buxassignment.model.order.DirectionType;
import com.alexeymatveev.buxassignment.model.order.SellOrderResponse;
import com.alexeymatveev.buxassignment.websocket.WebsocketClientEndpoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class TradingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradingBot.class);

    private OrderService orderService = new OrderService();

    private ObjectMapper objectMapper = new CommonObjectMapper();

    private List<OnCompleteHandler> onCompleteHandlerListeners = new CopyOnWriteArrayList<>();

    private List<OnProfitAndLossListener> onProfitAndLossListeners = new CopyOnWriteArrayList<>();

    private WebsocketClientEndpoint webSocketEndpoint;

    private String productId;

    private Float buyPrice;

    private Float lowerLimit;

    private Float upperLimit;

    private String currentPositionId;

    public TradingBot(WebsocketClientEndpoint webSocketEndpoint, String productId, Float buyPrice, Float lowerLimit, Float upperLimit) {
        this.webSocketEndpoint = webSocketEndpoint;
        this.productId = productId;
        this.buyPrice = buyPrice;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public void start() {
        webSocketEndpoint.addMessageHandler((message) -> {
            try {
                // get and parse the message
                BaseTMsg msg = objectMapper.readValue(message, BaseTMsg.class);

                // perform msg actions
                if (msg.getT() != MsgType.UNKNOWN) {
                    switch (msg.getT()) {
                        case CONNECT_CONNECTED:
                            // subscription successful - ready to receive messages
                            LOGGER.info("Web socket connection OK.");
                            SubscribeMsg subscribeMsg = MsgFactory.createSubscribeMsg(productId);
                            String subscribeMsgJson = objectMapper.writeValueAsString(subscribeMsg);
                            webSocketEndpoint.sendMessage(subscribeMsgJson);
                            break;
                        case CONNECT_FAILED:
                            // skip for now
                            LOGGER.error("Failure: " + msg.getBody());
                        case TRADING_QUOTE:
                            TradingQuoteMsg body = (TradingQuoteMsg) msg.getBody();
                            String currentProductId = body.getSecurityId();
                            // check price only for bot-configured product
                            if (currentProductId.equals(productId)) {
                                String currentPriceText = body.getCurrentPrice();
                                Float currentPrice = Float.parseFloat(currentPriceText);
                                // if price not set - buy first price
                                buyPrice = buyPrice == null ? currentPrice : buyPrice;
                                if (currentPositionId == null && buyPrice <= currentPrice) {
                                    // OK, buy order
                                    // as said: amount, leverage doesn't matter and direction=BUY always for now
                                    LOGGER.info("Buying order for product: " + productId);
                                    Result<BuyOrderResponse> buyOrderResult = orderService.buyOrder(productId, 200f, 1, DirectionType.BUY);
                                    if (buyOrderResult.isSuccessful()) {
                                        BuyOrderResponse buyOrderResponse = buyOrderResult.getData();
                                        // get currentPositionId and price
                                        LOGGER.info("Position opened for product: " + productId + " , price=" + buyOrderResponse.getPrice().getAmount());
                                        currentPositionId = buyOrderResponse.getPositionId();
                                    } else {
                                        LOGGER.error("Cannot buy order: " + buyOrderResult.getErrorMsg());
                                    }
                                } else {
                                    // check if price is outside bounds
                                    lowerLimit = lowerLimit == null ? buyPrice - 1: lowerLimit;
                                    upperLimit = upperLimit == null ? buyPrice + 1 : upperLimit;
                                    LOGGER.info("buy price = " + buyPrice + " , lower limit = " + lowerLimit
                                            + " , current price = " + currentPrice + " , " + " , upper limit = " + upperLimit );
                                    if (currentPrice < lowerLimit || currentPrice > upperLimit) {
                                        // if yes - close position and unsubscribe
                                        if (currentPrice < lowerLimit) {
                                            LOGGER.info("Price is lower then lower limit: " + currentPrice + " < " + lowerLimit + " - closing position");
                                        } else {
                                            LOGGER.info("Price is more then upper limit: " + currentPrice + " > " + upperLimit + " - closing position");
                                        }
                                        LOGGER.info("Selling order: " + currentPositionId);
                                        Result<SellOrderResponse> sellOrderResponseResult = orderService.sellOrder(currentPositionId);
                                        if (sellOrderResponseResult.isSuccessful()) {
                                            Float profitAndLoss = sellOrderResponseResult.getData().getProfitAndLoss().getAmount();
                                            onProfitAndLossListeners.forEach((handler) -> handler.onProfitAndLoss(profitAndLoss));
                                        }
                                        LOGGER.info("Unsubscribing from " + productId);
                                        SubscribeMsg unsubscribeMsg = MsgFactory.createUnsubscribeMsg(productId);
                                        String unsubscribeMsgJson = objectMapper.writeValueAsString(unsubscribeMsg);
                                        webSocketEndpoint.sendMessage(unsubscribeMsgJson);

                                        // notify listeners
                                        onCompleteHandlerListeners.forEach(OnCompleteHandler::onComplete);
                                    }
                                }
                            }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // subscribe to particular productId
        SubscribeMsg subscribeMsg = MsgFactory.createSubscribeMsg(productId);
        String subscribeMsgJson = null;
        try {
            subscribeMsgJson = objectMapper.writeValueAsString(subscribeMsg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        webSocketEndpoint.sendMessage(subscribeMsgJson);
    }

    public void stop() {
        LOGGER.info("Force stopping trading bot for position: " + currentPositionId);
        onCompleteHandlerListeners.clear();
    }

    public void addOnCompleteListener(OnCompleteHandler listener) {
        onCompleteHandlerListeners.add(listener);
    }

    public void addOnProfitAndLossListener(OnProfitAndLossListener listener) {
        onProfitAndLossListeners.add(listener);
    }

    public interface OnCompleteHandler {
        void onComplete();
    }

    public interface OnProfitAndLossListener {
        void onProfitAndLoss(Float profitAndLoss);
    }

}
