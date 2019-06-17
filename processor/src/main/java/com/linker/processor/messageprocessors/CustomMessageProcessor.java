package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CustomMessageProcessor extends MessageProcessor<Object> {

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doProcess(Message message, Object data, MessageContext context) throws IOException {

    }
}
