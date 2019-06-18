package com.linker.connector.messageprocessors;

import com.google.common.collect.ImmutableSet;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.connector.MessageService;
import com.linker.connector.WebSocketHandler;
import com.linker.connector.configurations.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class MessageProcessorService {
    @Autowired
    MessageService messageService;
    @Autowired
    ApplicationConfig applicationConfig;

    Set<MessageType> supportedIncomingMessageTypes = ImmutableSet.of(MessageType.AUTH_CLIENT, MessageType.AUTH_MASTER);

    public void processIncomingMessage(Message message, WebSocketHandler socketHandler) {
        log.info("start processing incoming message [{}]", message);
        MessageType type = null;
        String typeString = message.getContent().getType();
        if (EnumUtils.isValidEnum(MessageType.class, typeString)) {
            type = MessageType.valueOf(typeString);
            if (!supportedIncomingMessageTypes.contains(type)) {
                type = MessageType.ANY;
            }
        }

        MessageProcessor<?> processor = MessageProcessor.getProcessor(type);
        MessageContext messageContext = new MessageContext();
        messageContext.put("SOCKET_HANDLER", socketHandler);
        messageContext.put("DOMAIN_NAME", applicationConfig.getDomainName());
        messageContext.put("CONNECTOR_NAME", applicationConfig.getConnectorName());
        processor.process(message, messageContext);
    }
}
