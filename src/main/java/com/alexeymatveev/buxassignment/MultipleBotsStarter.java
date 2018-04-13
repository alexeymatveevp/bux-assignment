package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.service.SingleProductTradingBot;
import com.alexeymatveev.buxassignment.service.SubscriptionService;
import com.alexeymatveev.buxassignment.websocket.BUXWebsocketClientEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Starts multiple bots on different products in parallel.
 *
 * Created by Alexey Matveev on 4/12/2018.
 */
public class MultipleBotsStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitedBalanceBotStarter.class);

    private static String webSocketUrl = AppConfig.getInstance().getString("websocket.url");

    private static List<String> productIds = Arrays.asList(AppConfig.getInstance().getString("sample.product.ids").split(","));

    public static void main(String[] args) throws Exception {
        ReentrantLock lock = new ReentrantLock();
        Condition w8Connected = lock.newCondition();
        Condition w8FreeProduct = lock.newCondition();
        // init some stuff
        LoggingConfig.init();
        BUXWebsocketClientEndpoint buxEndpoint = new BUXWebsocketClientEndpoint(new URI(webSocketUrl));
        SubscriptionService subscriptionService = new SubscriptionService(buxEndpoint);

        AtomicInteger cycles = new AtomicInteger(10);

        // create a stack of products which are free to open position for
        Stack<String> freeProductsStack = new Stack<>();
        productIds.forEach(freeProductsStack::push);

        // connect to web socket and subscribe to all products
        lock.lock();
        buxEndpoint.addOnConnectedListener(() -> {
            lock.lock();
            productIds.forEach(subscriptionService::subscribe);
            w8Connected.signalAll();
            lock.unlock();
        });
        buxEndpoint.connect();
        w8Connected.await();
        lock.unlock();

        // loop
        while (cycles.intValue() > 0) {
            lock.lock();
            // wait for product without opened position
            if (freeProductsStack.isEmpty()) {
                LOGGER.info("Waiting for free product to open position for...");
                w8FreeProduct.await();
            }
            String productId = freeProductsStack.pop();

            // init the bot (prices will be auto-detected)
            SingleProductTradingBot bot = new SingleProductTradingBot(buxEndpoint, productId);

            // continue main thread after bot job is done
            bot.addOnCompleteListener(() -> {
                lock.lock();
                bot.stop();
                cycles.decrementAndGet();
                System.out.println("Positions to close left: " + cycles.intValue());
                freeProductsStack.push(bot.getProductId());
                w8FreeProduct.signalAll();
                lock.unlock();
            });

            // start bot
            bot.start();
            lock.unlock();
        }

        // unsubscribe
        productIds.forEach(subscriptionService::unsubscribe);

        // close the connection
        buxEndpoint.close();
    }

}
