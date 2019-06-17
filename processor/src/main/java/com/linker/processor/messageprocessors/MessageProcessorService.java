package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.processor.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageProcessorService {
    @Autowired
    MessageService messageService;

    public void process(Message message) {
        log.info("start processing message [{}]", message);
        MessageType messageType = MessageType.valueOf(message.getContent().getType());
        MessageProcessor<?> processor = MessageProcessor.getProcessor(messageType);
        processor.process(message, null);
    }
}
