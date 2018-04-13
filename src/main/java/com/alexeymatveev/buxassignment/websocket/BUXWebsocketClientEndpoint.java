package com.alexeymatveev.buxassignment.websocket;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.model.message.BaseTMsg;
import com.alexeymatveev.buxassignment.model.message.MsgType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class BUXWebsocketClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(BUXWebsocketClientEndpoint.class);

    private ObjectMapper objectMapper = new CommonObjectMapper();

    /** Web socket connection endpoint. */
    private URI endpointURI;

    /** Currently opened session. */
    private Session session;

    /** List of onMessage listeners. */
    private List<OnMessageHandler> onMessageHandlerListeners = new CopyOnWriteArrayList<>();

    /** List of onConnected listeners. */
    private List<OnConnectedListener> onConnectedListeners = new CopyOnWriteArrayList<>();

    /** Identifies whether "connect.conencted" message is already received. */
    private boolean connected = false;

    public BUXWebsocketClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    /**
     * Connects to web socket.
     */
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes connection.
     * @throws IOException
     */
    public void close() throws IOException {
        LOGGER.debug("Closing web socket connection.");
        this.session.close();
    }

    /**
     * Sends the message over web socket.
     * @param message
     */
    public void sendMessage(String message) {
        LOGGER.debug("Sending message: "+ message);
        session.getAsyncRemote().sendText(message);
    }

    public boolean isConnected() {
        return connected;
    }

    @OnOpen
    public void onOpen(Session userSession) {
        LOGGER.debug("Opening websocket to " + userSession.getRequestURI().toString());
        session = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOGGER.debug("Closing websocket: " + reason.toString() + " " + reason.getReasonPhrase() + " " + reason.getCloseCode().toString());
        session = null;
    }

    /**
     * Waits for connected message and when received starts notifying listeners.
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        LOGGER.debug("Receiving message: " + message);
        if (!connected) {
            // if not connected w8 for connected message
            try {
                BaseTMsg msg = objectMapper.readValue(message, BaseTMsg.class);
                if (msg.getT() == MsgType.CONNECT_CONNECTED) {
                    LOGGER.info("BUX web socket connection OK.");
                    connected = true;
                    onConnectedListeners.forEach(OnConnectedListener::onConnected);
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        } else {
            // parse the message
            try {
                BaseTMsg msg = objectMapper.readValue(message, BaseTMsg.class);
                onMessageHandlerListeners.forEach((listener) -> listener.handleMessage(msg));
            } catch (IOException e) {
                LOGGER.error("Cannot parse message: " + message);
            }
        }
    }

    @OnError
    public void processError(Throwable t) {
        LOGGER.error("", t);
    }

    public void addMessageListener(OnMessageHandler msgHandler) {
        onMessageHandlerListeners.add(msgHandler);
    }

    public void addOnConnectedListener(OnConnectedListener listener) {
        onConnectedListeners.add(listener);
    }

    public void removeMessageListener(OnMessageHandler handler) {
        onMessageHandlerListeners.remove(handler);
    }

    public interface OnMessageHandler {
        void handleMessage(BaseTMsg message);
    }

    public interface OnConnectedListener {
        void onConnected();
    }

}