package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.message.MsgFactory;
import com.alexeymatveev.buxassignment.model.message.SubscribeMsg;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexey Matveev on 4/12/2018.
 */
public class SubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    private ObjectMapper objectMapper = new CommonObjectMapper();

    private BUXWebsocketClientEndpoint endpoint;

    public SubscriptionService(BUXWebsocketClientEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Result<Void> subscribe(String productId) {
        // subscribe to particular productId
        if (endpoint == null) {
            return Result.fail("Null endpoint");
        }
        try {
            SubscribeMsg subscribeMsg = MsgFactory.createSubscribeMsg(productId);
            String subscribeMsgJson = objectMapper.writeValueAsString(subscribeMsg);
            LOGGER.info("Subscribing to " + productId);
            return sendRawMessage(subscribeMsgJson);
        } catch (JsonProcessingException e) {
            String msg = "Cannot create subscribe msg";
            LOGGER.error(msg, e);
            return Result.fail(msg);
        }
    }

    public Result<Void> unsubscribe(String productId) {
        // unsubscribe from particular productId
        if (endpoint == null) {
            return Result.fail("Null endpoint");
        }
        try {
            SubscribeMsg unsubscribeMsg = MsgFactory.createUnsubscribeMsg(productId);
            String unsubscribeMsgJson = objectMapper.writeValueAsString(unsubscribeMsg);
            LOGGER.info("Unsubscribing from " + productId);
            return sendRawMessage(unsubscribeMsgJson);
        } catch (JsonProcessingException e) {
            String msg = "Cannot create unsubscribe msg";
            LOGGER.error(msg, e);
            return Result.fail(msg);
        }
    }

    private Result<Void> sendRawMessage(String message) {
        try {
            endpoint.sendMessage(message);
        } catch (Exception e) {
            String msg = "Error sending message";
            LOGGER.error(msg, e);
            return Result.fail(msg);
        }
        return Result.ok();
    }

}
