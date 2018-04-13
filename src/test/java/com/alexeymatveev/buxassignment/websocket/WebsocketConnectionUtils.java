package com.alexeymatveev.buxassignment.websocket;

import com.alexeymatveev.buxassignment.config.AppConfig;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Alexey Matveev on 4/13/2018.
 */
public class WebsocketConnectionUtils {

    public static BUXWebsocketClientEndpoint connectToWebSocket(long time, TimeUnit timeUnit) throws Exception {
        String webSocketUrl = AppConfig.getInstance().getString("websocket.url");
        BUXWebsocketClientEndpoint endpoint = new BUXWebsocketClientEndpoint(new URI(webSocketUrl));
        ReentrantLock lock = new ReentrantLock();
        Condition w8Connect = lock.newCondition();
        lock.lock();
        endpoint.addOnConnectedListener(() -> {
            lock.lock();
            w8Connect.signalAll();
            lock.unlock();
        });
        endpoint.connect();
        // w8 30 seconds
        w8Connect.await(time, timeUnit);
        lock.unlock();
        return endpoint;
    }
}
