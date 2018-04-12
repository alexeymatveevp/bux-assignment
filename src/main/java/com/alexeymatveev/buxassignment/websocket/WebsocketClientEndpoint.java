package com.alexeymatveev.buxassignment.websocket;

import com.alexeymatveev.buxassignment.config.WebsocketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
@ClientEndpoint(configurator = WebsocketConfig.class)
public class WebsocketClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketClientEndpoint.class);

    private URI endpointURI;

    private Session session;

    private List<OnMessageHandler> onMessageHandlerListeners = new CopyOnWriteArrayList<>();

    private List<OnCloseHandler> onCloseHandlers = new CopyOnWriteArrayList<>();

    public WebsocketClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        LOGGER.debug("Closing web socket connection.");
        this.session.close();
    }

    @OnOpen
    public void onOpen(Session userSession) {
        LOGGER.debug("Opening websocket to " + userSession.getRequestURI().toString());
        session = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOGGER.debug("Closing websocket: " + reason.toString() + " " + reason.getReasonPhrase() + " " + reason.getCloseCode().toString());
        onCloseHandlers.forEach(OnCloseHandler::onClose);
        session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.debug("Receiving message: " + message);
        onMessageHandlerListeners.forEach((listener) -> listener.handleMessage(message));
    }

    @OnError
    public void processError(Throwable t) {
        LOGGER.error("", t);
    }

    public void addMessageHandler(OnMessageHandler msgHandler) {
        onMessageHandlerListeners.add(msgHandler);
    }

    public void removeMessageHandler(OnMessageHandler handler) {
        onMessageHandlerListeners.remove(handler);
    }

    public void addCloseListener(OnCloseHandler onCloseHandler) {
        this.onCloseHandlers.add(onCloseHandler);
    }

    public void sendMessage(String message) {
        LOGGER.debug("Sending message: "+ message);
        session.getAsyncRemote().sendText(message);
    }

    public interface OnMessageHandler {
        void handleMessage(String message);
    }

    public interface OnCloseHandler {
        void onClose();
    }
}