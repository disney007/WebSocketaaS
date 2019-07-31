package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CustomMessageProcessor extends PersistableMessageProcessor<MessageRequest> {

    PostOffice postOffice;

    @Autowired
    public CustomMessageProcessor(MessageRepository messageRepository, PostOffice postOffice) {
        super(messageRepository);
        this.postOffice = postOffice;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doPreprocess(Message message, MessageRequest data, MessageContext context) {
        message.setTo(data.getTo());
        super.doPreprocess(message, data, context);
    }

    @Override
    public void doProcess(Message message, MessageRequest data, MessageContext context) throws IOException {
        String from = message.getFrom();
        MessageForward messageData = new MessageForward(from, data.getContent());
        message.getContent().setData(messageData);

        updateMessageConfirmationFlag(message);

        try {
            postOffice.deliveryMessage(message);
        } catch (AddressNotFoundException e) {
            log.info("address not found for user [{}]", message.getTo(), e);
            updateMessageState(message, MessageState.TARGET_NOT_FOUND);
        }
    }

    void updateMessageConfirmationFlag(Message message) {
        MessageContent messageContent = message.getContent();
        boolean shouldConfirm = isMessagePersistable(message)
                || (messageContent.getConfirmationEnabled() && StringUtils.isNotBlank(messageContent.getReference()));
        message.getMeta().setConfirmEnabled(shouldConfirm);
    }
}
