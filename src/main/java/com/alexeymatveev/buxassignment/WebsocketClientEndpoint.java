package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.config.WebsocketConfig;
import com.alexeymatveev.buxassignment.model.message.MsgFactory;
import com.alexeymatveev.buxassignment.model.message.SubscribeMsg;
import com.alexeymatveev.buxassignment.util.JsonUtils;
import com.alexeymatveev.buxassignment.util.SomeData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
@ClientEndpoint(configurator = WebsocketConfig.class)
public class WebsocketClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketClientEndpoint.class);

    Session userSession;
    URI endpointURI;
    private MessageHandler messageHandler;

    ObjectMapper objectMapper = new CommonObjectMapper();

    private List<CloseListener> closeListeners = new CopyOnWriteArrayList<>();

    public WebsocketClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    public void connect() throws IOException {
        try {
//            final ClientManager client = ClientManager.createClient();
//            client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
//            client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);
//            client.getProperties().put(ClientProperties.INCOMING_BUFFER_SIZE, 1280000000);
//
//            userSession = client.connectToServer(this, endpointURI);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            container.setDefaultMaxBinaryMessageBufferSize(1280000000);
//            container.setDefaultMaxTextMessageBufferSize(1280000000);
//            Set<Extension> installedExtensions = container.getInstalledExtensions();
            userSession = container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket: " + reason.toString() + " " + reason.getReasonPhrase() + " " + reason.getCloseCode().toString());
        this.userSession = null;
        for (CloseListener closeListener : closeListeners) {
            closeListener.onClose();
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void addCloseListener(CloseListener closeListener) {
        this.closeListeners.add(closeListener);
    }

    public void sendMessage(String message) {
        LOGGER.debug("Sending message: "+ message);
        this.userSession.getAsyncRemote().sendText(message);
    }

    public interface MessageHandler {
        void handleMessage(String message);
    }

    public interface CloseListener {
        void onClose();
    }
}