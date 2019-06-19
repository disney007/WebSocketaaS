package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.connector.MessageService;
import com.linker.connector.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DefaultIncomingMessageProcessor extends IncomingMessageProcessor<Object> {

    @Autowired
    public DefaultIncomingMessageProcessor(MessageService messageService) {
        super(messageService);
    }

    @Override
    public void doProcess(Message message, Object data, SocketHandler socketHandler) throws IOException {
        this.messageService.sendMessage(message);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ANY;
    }
}
