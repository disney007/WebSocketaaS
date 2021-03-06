package com.linker.common.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketClientHandler extends SimpleChannelInboundHandler<MessageContentOutput> {

    private SocketClient socketClient;

    @Getter
    private ChannelHandlerContext context;

    public SocketClientHandler(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageContentOutput msg) throws Exception {
        this.socketClient.onMessage(msg);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
        log.info("socket client channel added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("socket client channel removed");
        this.socketClient.onDisconnected();
        this.context = null;
    }

    public void sendMessage(MessageContent msg) {
        if (this.context == null) {
            log.error("can not sent message, since context null");
            return;
        }
        this.context.writeAndFlush(msg);
    }
}
