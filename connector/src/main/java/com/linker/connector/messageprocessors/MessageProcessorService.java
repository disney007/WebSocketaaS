package com.linker.connector.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.connector.MessageService;
import com.linker.connector.SocketHandler;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.messageprocessors.incomming.IncomingMessageProcessor;
import com.linker.connector.messageprocessors.outgoing.OutgoingMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageProcessorService {
    @Autowired
    MessageService messageService;
    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    ApplicationContext applicationContext;

    private Map<MessageType, MessageProcessor> incomingMessageProcessors = new HashMap<>();
    private Map<MessageType, MessageProcessor> outgoingMessageProcessors = new HashMap<>();

    @PostConstruct
    public void setup() {
        loadProcessors(incomingMessageProcessors, applicationContext.getBeansOfType(IncomingMessageProcessor.class));
        loadProcessors(outgoingMessageProcessors, applicationContext.getBeansOfType(OutgoingMessageProcessor.class));
    }

    void loadProcessors(Map<MessageType, MessageProcessor> processors, Map<String, ? extends MessageProcessor> processorMap) {
        processorMap.values().forEach(processor -> processors.put(processor.getMessageType(), processor));
    }

    MessageProcessor<?> getProcessor(Message message, Map<MessageType, MessageProcessor> processorMap) {
        MessageType type = null;
        String typeString = message.getContent().getType();
        if (EnumUtils.isValidEnum(MessageType.class, typeString)) {
            type = MessageType.valueOf(typeString);
            if (!processorMap.containsKey(type))
                type = MessageType.ANY;
        }

        return processorMap.get(type);
    }

    public void processIncomingMessage(Message message, SocketHandler socketHandler) {
        log.info("start processing incoming message [{}]", message);
        MessageProcessor<?> processor = getProcessor(message, incomingMessageProcessors);
        MessageContext messageContext = new MessageContext();
        messageContext.put("SOCKET_HANDLER", socketHandler);
        messageContext.put("DOMAIN_NAME", applicationConfig.getDomainName());
        messageContext.put("CONNECTOR_NAME", applicationConfig.getConnectorName());
        processor.process(message, messageContext);
    }

    public void processOutgoingMessage(Message message) {
        log.info("start processing outgoing message [{}]", message);
        MessageContext messageContext = new MessageContext();
        messageContext.put("DOMAIN_NAME", applicationConfig.getDomainName());
        messageContext.put("CONNECTOR_NAME", applicationConfig.getConnectorName());
        MessageProcessor<?> processor = getProcessor(message, outgoingMessageProcessors);
        processor.process(message, messageContext);
    }
}
