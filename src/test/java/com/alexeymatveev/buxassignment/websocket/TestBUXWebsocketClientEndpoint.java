package com.alexeymatveev.buxassignment.websocket;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by Alexey Matveev on 4/13/2018.
 */
public class TestBUXWebsocketClientEndpoint {

    @Test
    public void testWaitForConnectMessage() throws Exception {
        BUXWebsocketClientEndpoint endpoint = WebsocketConnectionUtils.connectToWebSocket(30, TimeUnit.SECONDS);
        endpoint.close();

        assertTrue("No connect message for 30 seconds", endpoint.isConnected());
    }
}
