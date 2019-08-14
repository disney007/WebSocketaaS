package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.ClientAppService;
import lombok.extern.slf4j.Slf4j;
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
    ApplicationContext applicationContext;

    @Autowired
    ApplicationConfig applicationConfig;

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

    void processCrossDomainMessage(Message message) {
        if (message.getContent().getType() == MessageType.INTERNAL_MESSAGE) {
            process(message);
            return;
        }

        Message internalMessage = Message.builder()
                .from(message.getFrom())
                .to(message.getTo())
                .content(MessageUtils.createMessageContent(MessageType.INTERNAL_MESSAGE, message, message.getContent().getFeature()))
                .meta(new MessageMeta(message.getMeta().getOriginalAddress()))
                .build();
        process(internalMessage);
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

            if (message.getContent().getType() != MessageType.INTERNAL_MESSAGE && !processorUtils.isCurrentDomainMessage(message)) {
                log.info("message does not belong to current domain and send to domain [{}]", message);
                processCrossDomainMessage(message);
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

    public void persistMessage(Message message, MessageState state) {
        message.setState(state);
        if (message.getContent().getFeature() == MessageFeature.RELIABLE && state != MessageState.PROCESSED) {
            messageRepository.save(message);
        }
    }
}
