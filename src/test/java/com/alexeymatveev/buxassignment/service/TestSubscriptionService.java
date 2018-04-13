package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.model.message.MsgFactory;
import com.alexeymatveev.buxassignment.model.message.MsgType;
import com.alexeymatveev.buxassignment.model.message.SubscribeMsg;
import com.alexeymatveev.buxassignment.model.message.TradingQuoteMsg;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import com.alexeymatveev.buxassignment.websocket.WebsocketConnectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertTrue;

/**
 * Created by Alexey Matveev on 4/13/2018.
 */
public class TestSubscriptionService {

    private static List<String> productIds = Arrays.asList(AppConfig.getInstance().getString("sample.product.ids").split(","));

    private ObjectMapper objectMapper = new CommonObjectMapper();

    @Test
    public void testSubscribeToProduct() throws Exception {
        BUXWebsocketClientEndpoint endpoint = WebsocketConnectionUtils.connectToWebSocket(30, TimeUnit.SECONDS);
        String productId = productIds.get(0);
        SubscribeMsg subscribeMsg = MsgFactory.createSubscribeMsg(productId);
        ReentrantLock lock = new ReentrantLock();
        Condition w8Connect = lock.newCondition();
        AtomicBoolean tradingQuoteReceived = new AtomicBoolean();
        lock.lock();
        endpoint.addMessageListener((message -> {
            lock.lock();
            if (message.getT() == MsgType.TRADING_QUOTE) {
                TradingQuoteMsg body = (TradingQuoteMsg) message.getBody();
                if (body.getSecurityId().equals(productId)) {
                    tradingQuoteReceived.set(true);
                    w8Connect.signalAll();
                }
            }
            lock.unlock();
        }));
        endpoint.sendMessage(objectMapper.writeValueAsString(subscribeMsg));
        w8Connect.await(30, TimeUnit.SECONDS);
        lock.unlock();
        endpoint.close();

        assertTrue("Trading quote message for product " + productId + " was not received in 30 seconds", tradingQuoteReceived.get());
    }
}
