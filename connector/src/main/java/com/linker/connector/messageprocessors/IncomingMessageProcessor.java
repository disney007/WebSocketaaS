package com.linker.connector.messageprocessors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageMeta;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import com.linker.connector.MessageService;
import com.linker.connector.WebSocketHandler;
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
        MessageMeta meta = new MessageMeta();
        meta.setOriginalAddress(new Address(
                context.getValue("DOMAIN_NAME"),
                context.getValue("CONNECTOR_NAME")
        ));
        message.setMeta(meta);
        WebSocketHandler socketHandler = context.getValue("SOCKET_HANDLER");
        doProcess(message, data, socketHandler);
    }

    public abstract void doProcess(Message message, T data, WebSocketHandler webSocketHandler) throws IOException;
}
