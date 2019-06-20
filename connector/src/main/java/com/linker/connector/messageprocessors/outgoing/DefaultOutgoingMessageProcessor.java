package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageType;
import com.linker.connector.NetworkUserService;
import com.linker.connector.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DefaultOutgoingMessageProcessor extends OutgoingMessageProcessor<Object> {
    @Autowired
    NetworkUserService networkUserService;

    @Override
    public MessageType getMessageType() {
        return MessageType.ANY;
    }

    @Override
    public void doProcess(Message message, Object data, MessageContext context) throws IOException {
        SocketHandler user = networkUserService.getUser(message.getTo());
        if (user != null) {
            user.sendMessage(message);
        } else {
            String domainName = context.getValue("DOMAIN_NAME");
            String connectorName = context.getValue("CONNECTOR_NAME");
            log.warn("user [{}] not found on {} {}", message.getTo(), domainName, connectorName);
        }
    }
}
