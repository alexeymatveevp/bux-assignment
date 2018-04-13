package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.message.*;
import com.alexeymatveev.buxassignment.model.order.BuyOrderResponse;
import com.alexeymatveev.buxassignment.model.order.DirectionType;
import com.alexeymatveev.buxassignment.model.order.SellOrderResponse;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Single product trading bot.
 * Bot uses the {@link BUXWebsocketClientEndpoint} to subscribe for trading quote messages.
 *
 * Bot inputs:
 * - endpoint
 * - productId
 * - buyPrice (optional) - if not set the first price will be the buyPrice
 * - lowerLimit (optional) - if not set will be buyPrice - defaultPriceDeviation
 * - upperLimit (optional) - if not set will be buyPrice + defaultPriceDeviation
 *
 * Created by Alexey Matveev on 4/10/2018.
 */
public class SingleProductTradingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleProductTradingBot.class);

    /** If lower / upper is not set this fixed deviation from buy price will be used instead. */
    private float defaultPriceDeviation = (float) AppConfig.getInstance().getDouble("trading.bot.defaultPriceDeviation");

    private OrderService orderService = new OrderService();

    private List<OnCompleteListener> onCompleteListenerListeners = new CopyOnWriteArrayList<>();

    private List<OnProfitAndLossListener> onProfitAndLossListeners = new CopyOnWriteArrayList<>();

    /** The connected endpoint. */
    private BUXWebsocketClientEndpoint webSocketEndpoint;

    /** Bot can be started only once, holds the current state. */
    private boolean started = false;

    /** Holds the bot's message listener, after the bot ends it's job it will be removed from listeners list. */
    private BUXWebsocketClientEndpoint.OnMessageHandler botMessageListener;

    /** The product to trade. */
    private String productId;

    /** The buy price (can be null). */
    private Float buyPrice;

    /** The lower limit (can be null). */
    private Float lowerLimit;

    /** The upper limit (can be null). */
    private Float upperLimit;

    /** Currently opened position id (initially null) */
    private String currentPositionId = null;

    /**
     * Creates a new bot with auto-prices detection policy.
     */
    public SingleProductTradingBot(BUXWebsocketClientEndpoint webSocketEndpoint, String productId) {
        this.webSocketEndpoint = webSocketEndpoint;
        this.productId = productId;
    }

    public SingleProductTradingBot(BUXWebsocketClientEndpoint webSocketEndpoint, String productId, Float buyPrice, Float lowerLimit, Float upperLimit) {
        this.webSocketEndpoint = webSocketEndpoint;
        this.productId = productId;
        this.buyPrice = buyPrice;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    /**
     * Starts the bot, adds message listener to connected endpoint.
     */
    public void start() {
        if (started) {
            LOGGER.warn("Cannot start the same bot twice");
            return;
        }
        started = true;
        botMessageListener = (msg) -> {
            // interested only in trading quote messages
            if (msg.getT() == MsgType.TRADING_QUOTE) {
                TradingQuoteMsg body = (TradingQuoteMsg) msg.getBody();
                String currentProductId = body.getSecurityId();
                // bot tracks only single product
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
                        lowerLimit = lowerLimit == null ? buyPrice - defaultPriceDeviation : lowerLimit;
                        upperLimit = upperLimit == null ? buyPrice + defaultPriceDeviation : upperLimit;
                        LOGGER.info("productId = " + productId + " , buy price = " + buyPrice + " , lower limit = " + lowerLimit
                                + " , current price = " + currentPrice + " , upper limit = " + upperLimit);
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
                                LOGGER.info("Profit and loss: " + profitAndLoss);
                                onProfitAndLossListeners.forEach((handler) -> handler.onProfitAndLoss(profitAndLoss));
                            } else {
                                LOGGER.error("Error selling order: " + sellOrderResponseResult.getErrorMsg());
                            }
                            // notify listeners
                            onCompleteListenerListeners.forEach(OnCompleteListener::onComplete);
                        }
                    }
                }
            }
        };
        webSocketEndpoint.addMessageListener(botMessageListener);
    }

    public void stop() {
        LOGGER.info("Stopping trading bot for product: " + productId);
        webSocketEndpoint.removeMessageListener(botMessageListener);
    }

    public void addOnCompleteListener(OnCompleteListener listener) {
        onCompleteListenerListeners.add(listener);
    }

    public void addOnProfitAndLossListener(OnProfitAndLossListener listener) {
        onProfitAndLossListeners.add(listener);
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    public interface OnProfitAndLossListener {
        void onProfitAndLoss(Float profitAndLoss);
    }

    public String getProductId() {
        return productId;
    }
}
