package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.ClientAppService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageProcessorService {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    PostOffice postOffice;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ProcessorUtils processorUtils;

    @Autowired
    ClientAppService clientAppService;

    Map<MessageType, MessageProcessor<?>> processors = new HashMap<>();

    @PostConstruct
    public void setup() {
        applicationContext.getBeansOfType(MessageProcessor.class)
                .forEach((key, value) -> processors.put(value.getMessageType(), value));
    }

    public boolean isCurrentDomainMessage(Message message) {
        final String currentDomainName = applicationConfig.getDomainName();

        if (message.getMeta().getTargetAddress() != null) {
            return StringUtils.equalsIgnoreCase(currentDomainName, message.getMeta().getTargetAddress().getDomainName());
        }

        if (StringUtils.isBlank(message.getTo())) {
            return true;
        }

        String targetDomainName = clientAppService.resolveDomain(message.getTo());
        return StringUtils.equalsIgnoreCase(currentDomainName, targetDomainName);
    }

    void sendToRouter(Message message) {
        try {
            postOffice.deliveryMessage(message);
        } catch (IOException e) {
            log.error("deliver message failed", e);
            // TODO:save the message here
        }
    }

    public void process(Message message) {
        log.info("start processing message [{}]", message);

        MessageUtils.touchMessage(message);
        if (!MessageUtils.isMessageAlive(message)) {
            log.info("message [{}] is not alive", message);
            return;
        }

        MessageProcessor<?> processor = getMessageProcessor(message);
        if (processor != null) {
            processor.preprocess(message, null);

            if (processorUtils.isDomainProcessor() && !isCurrentDomainMessage(message)) {
                log.info("message does not belong to current domain and send to router [{}]", message);
                sendToRouter(message);
                return;
            }
            processor.process(message, null);
        } else {
            log.warn("no processor found for message {}", message);
        }
    }

    public MessageProcessor<?> getMessageProcessor(Message message) {
        MessageType messageType = message.getContent().getType();
        return processors.getOrDefault(messageType, null);
    }

    boolean isMessagePersistable(Message message) {
        MessageProcessor<?> processor = getMessageProcessor(message);
        if (processor instanceof PersistableMessageProcessor) {
            return ((PersistableMessageProcessor) processor).isMessagePersistable(message);
        }
        return false;
    }
}
