package com.linker.processor.messageprocessors;

import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.messages.FetchMissingMessagesComplete;
import com.linker.common.messages.FetchMissingMessagesRequest;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
@Slf4j
@RequiredArgsConstructor
public class FetchMissingMessagesMessageProcessor extends MessageProcessor<FetchMissingMessagesRequest> {

    public static final Integer MAX_COUNT = 2000;

    final MessageRepository messageRepository;

    final PostOffice postOffice;

    final RedisLockRegistry redisLockRegistry;

    @Override
    public MessageType getMessageType() {
        return MessageType.FETCH_MISSING_MESSAGES_REQUEST;
    }

    @Override
    public void doProcess(Message message, FetchMissingMessagesRequest data, MessageContext context) {
        Integer count = Math.min(MAX_COUNT, data.getCount());
        String toUser = message.getFrom();

        Lock lock = redisLockRegistry.obtain(toUser);
        lock.lock();
        try {
            Page<Message> page = messageRepository.findMessages(toUser, null,
                    ImmutableSet.of(MessageState.ADDRESS_NOT_FOUND, MessageState.NETWORK_ERROR), count);
            log.info("found {} missing messages for user [{}]", page.getTotalElements(), toUser);
            for (Message msg : page.getContent()) {
                postOffice.deliverMessage(msg);
                messageRepository.remove(msg);
            }

            sendCompleteMessage(message, page.getTotalElements() - page.getNumberOfElements());
        } finally {
            lock.unlock();
        }

    }

    void sendCompleteMessage(Message message, Long leftCount) {
        Message replyMessage = Message.builder()
                .content(MessageUtils.createMessageContent(MessageType.FETCH_MISSING_MESSAGES_COMPLETE,
                        new FetchMissingMessagesComplete(leftCount),
                        MessageFeature.RELIABLE))
                .from(Keywords.SYSTEM)
                .to(message.getFrom())
                .meta(message.getMeta())
                .build();
        postOffice.deliverMessage(replyMessage);
    }
}
