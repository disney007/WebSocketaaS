package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.connector.network.SocketHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class IncomingMessageProcessor<T> extends MessageProcessor<T> {

    public IncomingMessageProcessor() {
    }

    @Override
    public void process(Message message, MessageContext context) {
        try {
            super.process(message, context);
        } catch (Exception e) {
            log.error("processing incoming message failed {}", message, e);
            SocketHandler socketHandler = (SocketHandler) context.get("SOCKET_HANDLER");
            message.getContent().setType(MessageType.GENERAL_ERROR);
            socketHandler.sendMessage(message.getContent());
        }
    }

    @Override
    public void doProcess(Message message, T data, MessageContext context) throws IOException {
        SocketHandler socketHandler = context.getValue("SOCKET_HANDLER");
        doProcess(message, data, socketHandler);
    }

    public abstract void doProcess(Message message, T data, SocketHandler SocketHandler) throws IOException;
}
