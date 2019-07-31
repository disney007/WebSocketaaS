package com.linker.common.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SocketClient {
    Future<Void> connect(String host, int port);

    Future<?> close();

    ChannelFuture disconnect();

    void onConnected(Runnable connectedCallback);

    void sendMessage(MessageContent msg);

    void onMessage(Consumer<MessageContentOutput> msgCallback);

    void onMessage(MessageContentOutput msg);

    void onDisconnected(Runnable callback);

    void onDisconnected();
}
