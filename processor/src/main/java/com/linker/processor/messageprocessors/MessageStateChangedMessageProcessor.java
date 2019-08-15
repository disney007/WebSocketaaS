package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageConfirmation;
import com.linker.common.messages.MessageStateChanged;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.UserChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageStateChangedMessageProcessor extends MessageProcessor<MessageStateChanged> {

    final MessageRepository messageRepository;

    final MessageProcessorService messageProcessorService;

    final UserChannelService userChannelService;

    final PostOffice postOffice;

    final ApplicationConfig applicationConfig;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE_STATE_CHANGED;
    }

    @Override
    public void doProcess(Message message, MessageStateChanged data, MessageContext context) {

        if (data.getState() == MessageState.ADDRESS_NOT_FOUND) {
            processTargetNotFound(data.getMessage());
        } else {
            Message msg = data.getMessage();
            MessageState newState = data.getState();
            log.info("change message [{}] state to [{}]", msg.getId(), newState);
            messageProcessorService.persistMessage(msg, newState);

            if (newState == MessageState.PROCESSED
                    && msg.getContent().getType() == MessageType.MESSAGE
                    && StringUtils.equalsIgnoreCase(msg.getMeta().getTargetAddress().getDomainName(), applicationConfig.getDomainName())) {
                sendConfirmationMessageToSender(msg);
            }
        }
    }

    void processTargetNotFound(Message message) {
        /*
         * 1. remove invalid address.
         * 2. try to send one time, if still not found, state will be updated by post office
         * */
        String to = message.getTo();
        Address targetAddress = message.getMeta().getTargetAddress();
        userChannelService.removeAddress(to, targetAddress);
        log.info("remove invalid address [{}] from user [{}]", targetAddress, to);

        postOffice.deliverMessage(message);
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
