package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.messages.MessageConfirmation;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CustomMessageConfirmationProcessor extends PersistableMessageProcessor<MessageConfirmation> {

    PostOffice postOffice;

    @Autowired
    public CustomMessageConfirmationProcessor(MessageRepository messageRepository, PostOffice postOffice) {
        super(messageRepository);
        this.postOffice = postOffice;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE_CONFIRMATION;
    }

    @Override
    public void doProcess(Message message, MessageConfirmation data, MessageContext context) throws IOException {

        try {
            postOffice.deliveryMessage(message);
        } catch (AddressNotFoundException e) {
            log.info("address not found for user [{}]", message.getTo(), e);
            updateMessageState(message, MessageState.TARGET_NOT_FOUND);
        }

    }
}
