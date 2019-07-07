package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageSnapshot;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.processor.PostOffice;
import com.linker.processor.repositories.MessageRepository;
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
    PostOffice postOffice;

    @Autowired
    MessageRepository messageRepository;

    Map<MessageType, MessageProcessor<?>> processors = new HashMap<>();

    @PostConstruct
    public void setup() {
        applicationContext.getBeansOfType(MessageProcessor.class)
                .forEach((key, value) -> processors.put(value.getMessageType(), value));
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
