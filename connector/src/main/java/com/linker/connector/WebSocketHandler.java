package com.linker.connector;

import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.Utils;
import com.linker.connector.messageprocessors.MessageProcessorService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> implements SocketHandler {

    @Autowired
    MessageProcessorService messageProcessorService;
    ChannelHandlerContext context;
    String userId;
    public static WebSocketHandler instance;

    public WebSocketHandler() {
        instance = this;
    }

    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String msgContentJson = msg.text();
        MessageContent msgContent = Utils.fromJson(msgContentJson, MessageContent.class);

        Message message = Message.builder()
                .content(msgContent)
                .from("abc")
                .build();

        this.messageProcessorService.processIncomingMessage(message, this);
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

    public void sendMessage(String message) {
        context.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    public static void sendMessage0(String message) {
        instance.context.writeAndFlush(new TextWebSocketFrame(message));
    }
}
