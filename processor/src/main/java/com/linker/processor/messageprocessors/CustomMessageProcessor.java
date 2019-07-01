package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CustomMessageProcessor extends MessageProcessor<MessageRequest> {

    @Autowired
    PostOffice postOffice;

    @Autowired
    MessageProcessorService messageProcessorService;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doPreprocess(Message message, MessageRequest data, MessageContext context) {
        message.setTo(data.getTo());
    }

    @Override
    public void doProcess(Message message, MessageRequest data, MessageContext context) throws IOException {
        String from = message.getFrom();
        MessageForward messageData = new MessageForward(from, data.getContent());
        message.getContent().setData(messageData);

        try {
            postOffice.deliveryMessage(message);
        } catch (AddressNotFoundException e) {
            log.info("address not found for user [{}]", message.getTo());
            messageProcessorService.updateMessageState(message, MessageState.TARGET_NOT_FOUND);
        }
    }
}
