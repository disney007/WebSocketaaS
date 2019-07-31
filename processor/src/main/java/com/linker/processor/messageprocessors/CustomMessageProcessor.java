package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.express.PostOffice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomMessageProcessor extends MessageProcessor<MessageRequest> {

    final PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doPreprocess(Message message, MessageRequest data, MessageContext context) {
        message.setTo(data.getTo());
    }

    @Override
    public void doProcess(Message message, MessageRequest data, MessageContext context) {
        String from = message.getFrom();
        MessageForward messageData = new MessageForward(from, data.getContent());
        message.getContent().setData(messageData);

        updateMessageConfirmationFlag(message);
        postOffice.deliverMessage(message);

    }

    void updateMessageConfirmationFlag(Message message) {
        MessageContent messageContent = message.getContent();
        boolean shouldConfirm = messageContent.getConfirmationEnabled() && StringUtils.isNotBlank(messageContent.getReference());
        message.getMeta().setConfirmEnabled(shouldConfirm);
    }
}
