package com.linker.connector;

import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.Utils;
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
        String msgContentJson = msg.text();
        MessageContent msgContent = Utils.fromJson(msgContentJson, MessageContent.class);

        Message message = Message.builder()
                .content(msgContent)
                .from("abc")
                .build();

        this.messageService.sendMessage(message);
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
