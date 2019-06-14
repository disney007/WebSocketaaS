package com.linker.connector;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    MessageService messageService;
    ChannelHandlerContext context;
    public static WebSocketHandler instance;

    public WebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
        instance = this;
    }

    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        this.messageService.sendMessage(msg.text());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
        log.info("channel added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("channel removed");
    }

    public static void sendMessage(String message) {
        instance.context.writeAndFlush(new TextWebSocketFrame(message));
    }
}
