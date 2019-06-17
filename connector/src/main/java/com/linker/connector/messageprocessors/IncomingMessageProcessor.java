package com.linker.connector.messageprocessors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import com.linker.connector.MessageService;
import com.linker.connector.WebSocketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class IncomingMessageProcessor<T> extends MessageProcessor<T> {
    protected MessageService messageService;

    public IncomingMessageProcessor(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void process(Message message, MessageContext context) {
        try {
            super.process(message, context);
        } catch (Exception e) {
            log.error("processing incoming message failed {}", message, e);
            WebSocketHandler socketHandler = (WebSocketHandler) context.get("SOCKET_HANDLER");
            message.getContent().setType(MessageType.GENERAL_ERROR.name());
            try {
                socketHandler.sendMessage(Utils.toJson(message.getContent()));
            } catch (JsonProcessingException ex) {
                log.error("send message failed", ex);
            }
        }
    }

    @Override
    public void doProcess(Message message, T data, MessageContext context) throws IOException {
        doProcess(message, data, (WebSocketHandler) context.get("SOCKET_HANDLER"));
    }

    public abstract void doProcess(Message message, T data, WebSocketHandler webSocketHandler) throws IOException;
}
