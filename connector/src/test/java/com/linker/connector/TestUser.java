package com.linker.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            user.onMessage(s);
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

    @Getter
    @Setter
    String username;
    @Getter
    @Setter
    Long socketId;
    WebSocketClient webSocketClient;
    Consumer<ServerHandshake> onConnectedCallback;
    Consumer<Exception> onErrorCallback;
    Consumer<String> onCloseCallback;
    Consumer<String> onMessageCallback;
    LinkedBlockingQueue<MessageContentOutput> receivedMessageQueue = new LinkedBlockingQueue<>();

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
        try {
            MessageContentOutput testMessage = Utils.fromJson(message, MessageContentOutput.class);
            this.receivedMessageQueue.add(testMessage);
            if (onMessageCallback != null) {
                this.onMessageCallback.accept(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        this.webSocketClient = null;
        if (this.onMessageCallback != null) {
            this.onCloseCallback.accept(str);
        }
    }

    public void onClosed(Consumer<String> consumer) {
        this.onCloseCallback = consumer;
    }

    public void send(MessageContent message) {
        try {
            String json = Utils.toJson(message);
            this.webSocketClient.send(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to convert test message to json", e);
        }
    }

    public void close() {
        log.info("[{}] closed", this.username);
        if (this.webSocketClient != null) {
            this.webSocketClient.close();
        }

        this.webSocketClient = null;
    }

    public boolean isClosed() {
        return webSocketClient == null;
    }

    public MessageContentOutput getReceivedMessage() throws InterruptedException {
        return this.receivedMessageQueue.poll(3L, TimeUnit.SECONDS);
    }

    public MessageContentOutput getReceivedMessage(MessageType type) throws TimeoutException {
        log.info("test user [{}]:waiting for message {}", username, type);
        MessageContentOutput testMessage;
        do {
            try {
                testMessage = getReceivedMessage();
            } catch (InterruptedException e) {
                log.error("test user [{}] waiting for message {} interrupted", username, type);
                testMessage = null;
            }

            if (testMessage == null) {
                throw new TimeoutException(String.format("test user [%s]: failed to get message %s", username, type));
            }
        } while (testMessage.getType() != type);
        return testMessage;
    }
}
