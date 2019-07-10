package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageContext;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.GroupMessage;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.repositories.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CustomGroupMessageProcessor extends PersistableMessageProcessor<GroupMessage> {

    MessageProcessorService messageProcessorService;

    public CustomGroupMessageProcessor(MessageRepository messageRepository, MessageProcessorService messageProcessorService) {
        super(messageRepository);
        this.messageProcessorService = messageProcessorService;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.GROUP_MESSAGE;
    }

    @Override
    public void doProcess(Message message, GroupMessage data, MessageContext context) throws IOException {
        data.getTo().forEach(toUser -> {
            MessageContent messageContent = MessageUtils.createMessageContent(MessageType.MESSAGE,
                    new MessageRequest(toUser, data.getContent()), message.getContent().getReference(), message.getContent().getFeature());
            messageContent.setConfirmationEnabled(false);

            Message subMessage = Message.builder()
                    .from(message.getFrom())
                    .to(toUser)
                    .meta(message.getMeta())
                    .state(MessageState.CREATED)
                    .createdAt(message.getCreatedAt())
                    .content(messageContent)
                    .build();
            messageProcessorService.process(subMessage);
        });
        updateMessageState(message, MessageState.PROCESSED);
    }
}
