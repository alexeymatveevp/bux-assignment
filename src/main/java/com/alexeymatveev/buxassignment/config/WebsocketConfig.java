package com.alexeymatveev.buxassignment.config;

import javax.websocket.ClientEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class WebsocketConfig extends ClientEndpointConfig.Configurator {

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        String authToken = AppConfig.getInstance().getString("authorization.token");
        headers.putIfAbsent("Authorization", Collections.singletonList(authToken));
        headers.putIfAbsent("Accept-Language", Collections.singletonList("nl-NL,en;q=0.8"));
        super.beforeRequest(headers);
    }
}
