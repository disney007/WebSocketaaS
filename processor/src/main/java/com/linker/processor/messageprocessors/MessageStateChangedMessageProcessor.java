package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageSnapshot;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.messages.MessageStateChanged;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class MessageStateChangedMessageProcessor extends MessageProcessor<MessageStateChanged> {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageProcessorService messageProcessorService;

    @Autowired
    UserChannelService userChannelService;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE_STATE_CHANGED;
    }

    @Override
    public void doProcess(Message message, MessageStateChanged data, MessageContext context) throws IOException {

        if (data.getState() == MessageState.TARGET_NOT_FOUND) {
            processTargetNotFound(message, data);
        } else {
            MessageSnapshot msg = data.getMessage();
            log.info("change message [{}] state to [{}]", msg.getId(), data.getState());
            messageProcessorService.updateMessageState(msg.getFeature(), msg.getType(), msg.getId(), data.getState());
        }
    }

    void processTargetNotFound(Message message, MessageStateChanged data) {
        /*
         * 1. remove invalid address.
         * 2. try to send one time, if still not found, state will be updated by post office
         * */
        userChannelService.removeAddress(data.getMessage().getTo(), message.getMeta().getOriginalAddress());
        Message originalMessage = messageRepository.findById(data.getMessage().getId());
        messageProcessorService.process(originalMessage);
    }
}
