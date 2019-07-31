package com.linker.processor.messageprocessors;

import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.messages.MessageConfirmation;
import com.linker.common.messages.MessageStateChanged;
import com.linker.processor.express.PostOffice;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    PostOffice postOffice;

    @Autowired
    ApplicationConfig applicationConfig;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE_STATE_CHANGED;
    }

    @Override
    public void doProcess(Message message, MessageStateChanged data, MessageContext context) throws IOException {

        if (data.getState() == MessageState.TARGET_NOT_FOUND) {
            processTargetNotFound(message, data);
        } else {
            Message msg = data.getMessage();
            MessageState newState = data.getState();
            log.info("change message [{}] state to [{}]", msg.getId(), newState);
            if (messageProcessorService.isMessagePersistable(msg)) {
                messageRepository.updateState(msg.getId(), newState);
            }

            if (newState == MessageState.PROCESSED && msg.getContent().getType() == MessageType.MESSAGE) {
                sendConfirmationMessageToSender(msg);
            }
        }
    }

    void processTargetNotFound(Message message, MessageStateChanged data) throws IOException {
        /*
         * 1. remove invalid address.
         * 2. try to send one time, if still not found, state will be updated by post office
         * */
        String to = data.getMessage().getTo();
        Address originalAddress = message.getMeta().getOriginalAddress();
        userChannelService.removeAddress(to, originalAddress);
        log.info("remove address [{}] from user [{}]", message.getMeta().getOriginalAddress(), to);

        Message originalMessage = messageRepository.findById(data.getMessage().getId());
        try {
            if (originalMessage != null) {
                postOffice.deliveryMessage(originalMessage);
            }
        } catch (AddressNotFoundException e) {
            messageRepository.updateState(originalMessage.getId(), MessageState.TARGET_NOT_FOUND);
        }
    }

    void sendConfirmationMessageToSender(Message message) {
        String reference = message.getContent().getReference();
        if (StringUtils.isNotBlank(reference) && message.getContent().getConfirmationEnabled()) {
            log.info("send confirmation back for message {}", message);
            Message confirmMessage = Message.builder()
                    .from(Keywords.SYSTEM)
                    .to(message.getFrom())
                    .content(MessageUtils.createMessageContent(MessageType.MESSAGE_CONFIRMATION, new MessageConfirmation(reference), MessageFeature.RELIABLE))
                    .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName())))
                    .build();
            messageProcessorService.process(confirmMessage);
        }
    }
}
