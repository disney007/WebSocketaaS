package com.linker.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.Utils;
import com.linker.common.exceptions.ProcessMessageException;
import com.linker.common.models.UserDisconnectedMessage;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.messageprocessors.MessageProcessorService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> implements SocketHandler {

    @Autowired
    MessageProcessorService messageProcessorService;
    @Autowired
    ApplicationConfig applicationConfig;

    ChannelHandlerContext context;

    @Getter
    @Setter
    String userId;

    @Getter
    @Setter
    AuthStatus authStatus = AuthStatus.NOT_AUTHENTICATED;

    @Getter
    Long socketId;
    public static WebSocketHandler instance;

    public WebSocketHandler(Long socketId) {
        instance = this;
        this.socketId = socketId;
    }

    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String msgContentJson = msg.text();
        MessageContent msgContent = Utils.fromJson(msgContentJson, MessageContent.class);

        MessageMeta meta = new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), this.socketId));
        Message message = Message.builder()
                .content(msgContent)
                .from(this.userId)
                .meta(meta)
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
        if (this.authStatus == AuthStatus.NOT_AUTHENTICATED) {
            return;
        }
        MessageMeta meta = new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), socketId));
        Message message = Message.builder()
                .content(
                        MessageUtils.createMessageContent(MessageType.USER_DISCONNECTED, new UserDisconnectedMessage(this.userId)
                                , MessageFeature.RELIABLE)
                )
                .from(Keywords.SYSTEM)
                .meta(meta)
                .build();
        this.messageProcessorService.processIncomingMessage(message, this);
    }

    public ChannelFuture sendMessage(String message) {
        return context.writeAndFlush(new TextWebSocketFrame(message));
    }

    public ChannelFuture sendMessage(Message message) {
        try {
            return sendMessage(Utils.toJson(message.getContent()));
        } catch (JsonProcessingException e) {
            String msg = String.format("failed to convert message content to json %s", message);
            throw new ProcessMessageException(msg, e);
        }
    }

    public ChannelFuture close() {
        return this.context.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception received", cause);
    }
}
