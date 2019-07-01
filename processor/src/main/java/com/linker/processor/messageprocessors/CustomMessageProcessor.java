package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.models.MessageForwardMessage;
import com.linker.common.models.MessageRequestMessage;
import com.linker.processor.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CustomMessageProcessor extends MessageProcessor<MessageRequestMessage> {

    @Autowired
    PostOffice postOffice;

    @Autowired
    MessageProcessorService messageProcessorService;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doProcess(Message message, MessageRequestMessage data, MessageContext context) throws IOException {
        String from = message.getFrom();
        MessageForwardMessage messageData = new MessageForwardMessage(from, data.getContent());
        message.getContent().setData(messageData);
        message.setTo(data.getTo());

        try {
            postOffice.deliveryMessage(message);
        } catch (AddressNotFoundException e) {
            log.info("address not found for user [{}]", message.getTo());
            messageProcessorService.updateMessageState(message, MessageState.TARGET_NOT_FOUND);
        }
    }
}
