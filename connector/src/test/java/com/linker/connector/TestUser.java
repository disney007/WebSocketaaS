package com.linker.connector;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

@Slf4j
public class TestUser {
    static class TestWebSocketClient extends WebSocketClient {

        TestUser user;

        public TestWebSocketClient(URI serverURI, TestUser user) {
            super(serverURI);
            this.user = user;
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            user.onConnected(serverHandshake);
        }

        @Override
        public void onMessage(String s) {
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            user.onClosed(s);
        }

        @Override
        public void onError(Exception e) {
            user.onError(e);
        }
    }

    String username;
    WebSocketClient webSocketClient;
    Consumer<ServerHandshake> onConnectedCallback;
    Consumer<Exception> onErrorCallback;
    Consumer<String> onCloseCallback;
    Consumer<String> onMessageCallback;

    public TestUser(String username) {
        this.username = username;
    }

    public void connect() {
        try {
            log.info("user [{}] is connecting to ws", this.username);
            webSocketClient = new TestWebSocketClient(new URI("ws://localhost:18089/ws"), this);
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            log.error("invalid url", e);
        }
    }

    void onMessage(String message) {
        log.info("user [{}] received message {}", username, message);
        this.onMessageCallback.accept(message);
    }

    public void onMessage(Consumer<String> consumer) {
        this.onMessageCallback = consumer;
    }

    void onConnected(ServerHandshake serverHandshake) {
        log.info("user [{}] connected to ws", this.username);
        if (onConnectedCallback != null) {
            onConnectedCallback.accept(serverHandshake);
        }
    }

    public void onConnected(Consumer<ServerHandshake> consumer) {
        onConnectedCallback = consumer;
    }

    void onError(Exception e) {
        log.error("error received from user [{}]", username, e);
        if (onErrorCallback != null) {
            onErrorCallback.accept(e);
        }
    }

    public void onError(Consumer<Exception> consumer) {
        this.onErrorCallback = consumer;
    }

    void onClosed(String str) {
        log.info("user [{}] closed", username);
        this.onCloseCallback.accept(str);
    }

    public void onClosed(Consumer<String> consumer) {
        this.onCloseCallback = consumer;
    }
}
