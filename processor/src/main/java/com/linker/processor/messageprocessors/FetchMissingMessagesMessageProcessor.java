package com.linker.processor.messageprocessors;

import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageFeature;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.FetchMissingMessagesComplete;
import com.linker.common.messages.FetchMissingMessagesRequest;
import com.linker.processor.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class FetchMissingMessagesMessageProcessor extends MessageProcessor<FetchMissingMessagesRequest> {
    public static final Integer MAX_COUNT = 100;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageProcessorService messageProcessorService;

    @Autowired
    PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.FETCH_MISSING_MESSAGES_REQUEST;
    }

    @Override
    public void doProcess(Message message, FetchMissingMessagesRequest data, MessageContext context) throws IOException {
        Integer count = Math.min(MAX_COUNT, data.getCount());
        String toUser = message.getFrom();
        Page<Message> page = messageRepository.findMessages(toUser, MessageType.MESSAGE, MessageState.TARGET_NOT_FOUND, count);
        log.info("found {} missing messages for user [{}]", page.getTotalElements(), toUser);
        page.get().forEach(msg -> messageProcessorService.process(msg));

        sendCompleteMessage(message, page.getTotalElements() - page.getNumberOfElements());
    }

    void sendCompleteMessage(Message message, Long leftCount) throws IOException {
        Message replyMessage = Message.builder()
                .content(MessageUtils.createMessageContent(MessageType.FETCH_MISSING_MESSAGES_COMPLETE,
                        new FetchMissingMessagesComplete(leftCount),
                        MessageFeature.RELIABLE))
                .from(Keywords.SYSTEM)
                .to(message.getFrom())
                .meta(message.getMeta())
                .build();
        postOffice.deliveryMessage(replyMessage);
    }
}
