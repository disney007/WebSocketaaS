package com.linker.common.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class TcpSocketClient implements SocketClient {

    private final static Integer MAX_CONTENT_LENGTH = 8192;
    private final static Integer NEXT_RETRY_DELAY = 5;

    Runnable connectedCallback;
    Consumer<MessageContentOutput> msgCallback;

    EventLoopGroup workerGroup;
    Bootstrap bootstrap;
    String host;
    int port;
    @Setter
    SocketClientHandler socketClientHandler;

    static class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

        TcpSocketClient tcpSocketClient;

        public TcpChannelInitializer(TcpSocketClient tcpSocketClient) {
            this.tcpSocketClient = tcpSocketClient;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_CONTENT_LENGTH, 0, 4, 0, 4));
            pipeline.addLast(new LengthFieldPrepender(4));
            pipeline.addLast(new ByteArrayDecoder());
            pipeline.addLast(new ByteArrayEncoder());
            pipeline.addLast(new TcpSocketClientCodec());

            SocketClientHandler socketClientHandler = new SocketClientHandler(tcpSocketClient);
            tcpSocketClient.setSocketClientHandler(socketClientHandler);
            pipeline.addLast(socketClientHandler);
        }
    }

    public TcpSocketClient() {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new TcpChannelInitializer(this));

    }

    public Future<Void> connect(String host, int port) {
        this.host = host;
        this.port = port;
        return doConnect();
    }

    public void close() {
        log.info("close tcp socket client");
        workerGroup.shutdownGracefully();
    }

    Future<Void> doConnect() {
        log.info("starting tcp socket client");
        return bootstrap.connect(host, port).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                log.info("tcp socket connected");
                if (connectedCallback != null) {
                    connectedCallback.run();
                }
            } else {
                log.info("connection failed, retry in {} seconds", NEXT_RETRY_DELAY);
                future.channel().eventLoop().schedule(this::doConnect, NEXT_RETRY_DELAY, TimeUnit.SECONDS);
            }
        });
    }

    public void onConnected(Runnable connectedCallback) {
        this.connectedCallback = connectedCallback;
    }

    @Override
    public void sendMessage(MessageContent msg) {
        this.socketClientHandler.sendMessage(msg);
    }

    @Override
    public void onMessage(Consumer<MessageContentOutput> msgCallback) {
        this.msgCallback = msgCallback;
    }

    @Override
    public void onMessage(MessageContentOutput msg) {
        if (this.msgCallback != null) {
            this.msgCallback.accept(msg);
        }
    }
}
