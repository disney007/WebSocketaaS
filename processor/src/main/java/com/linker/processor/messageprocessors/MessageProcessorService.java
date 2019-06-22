package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.processor.PostOffice;
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

    Map<MessageType, MessageProcessor<?>> processors = new HashMap<>();

    @PostConstruct
    public void setup() {
        applicationContext.getBeansOfType(MessageProcessor.class)
                .forEach((key, value) -> processors.put(value.getMessageType(), value));
    }

    public void process(Message message) {
        log.info("start processing message [{}]", message);
        MessageType messageType = MessageType.valueOf(message.getContent().getType());
        MessageProcessor<?> processor = processors.getOrDefault(messageType, null);
        if (processor != null) {
            processor.process(message, null);
        } else {
            log.warn("no processor found for message type {}", messageType);
        }
    }
}
