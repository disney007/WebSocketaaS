package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.messages.MessageConfirmation;
import com.linker.processor.express.PostOffice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomMessageConfirmationProcessor extends MessageProcessor<MessageConfirmation> {

    final PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE_CONFIRMATION;
    }

    @Override
    public void doProcess(Message message, MessageConfirmation data, MessageContext context) {
        postOffice.deliverMessage(message);
    }
}
