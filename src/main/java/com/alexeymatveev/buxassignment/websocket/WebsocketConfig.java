package com.alexeymatveev.buxassignment.websocket;

import com.alexeymatveev.buxassignment.config.AppConfig;

import javax.websocket.ClientEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Web socket config. Sets security header.
 *
 * Created by Alexey Matveev on 4/10/2018.
 */
public class WebsocketConfig extends ClientEndpointConfig.Configurator {

    private static String authToken = AppConfig.getInstance().getString("authorization.token");

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.putIfAbsent("Authorization", Collections.singletonList(authToken));
        headers.putIfAbsent("Accept-Language", Collections.singletonList("nl-NL,en;q=0.8"));
        super.beforeRequest(headers);
    }
}
