package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.GroupMessage;
import com.linker.common.messages.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomGroupMessageProcessor extends MessageProcessor<GroupMessage> {

    final MessageProcessorService messageProcessorService;

    @Override
    public MessageType getMessageType() {
        return MessageType.GROUP_MESSAGE;
    }

    @Override
    public void doProcess(Message message, GroupMessage data, MessageContext context) {
        data.getTo().forEach(toUser -> {
            MessageContent messageContent = MessageUtils.createMessageContent(MessageType.MESSAGE,
                    new MessageRequest(toUser, data.getContent()), message.getContent().getReference(), message.getContent().getFeature());
            messageContent.setConfirmationEnabled(false);

            Message subMessage = Message.builder()
                    .from(message.getFrom())
                    .to(toUser)
                    .meta(message.getMeta().clone())
                    .state(MessageState.CREATED)
                    .createdAt(message.getCreatedAt())
                    .content(messageContent)
                    .build();
            messageProcessorService.process(subMessage);
        });
    }
}
