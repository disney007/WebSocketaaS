package com.linker.common.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SocketClient {
    Future<Void> connect(String host, int port);

    void close();

    void onConnected(Runnable connectedCallback);

    void sendMessage(MessageContent msg);

    void onMessage(Consumer<MessageContentOutput> msgCallback);

    void onMessage(MessageContentOutput msg);
}
