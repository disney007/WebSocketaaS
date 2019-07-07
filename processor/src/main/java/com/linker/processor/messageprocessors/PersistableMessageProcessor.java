package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageFeature;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageState;
import com.linker.processor.repositories.MessageRepository;

public abstract class PersistableMessageProcessor<T> extends MessageProcessor<T> {

    MessageRepository messageRepository;

    public PersistableMessageProcessor(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public boolean isMessagePersistable(Message message) {
        return message.getContent().getFeature() == MessageFeature.RELIABLE;
    }

    public void persistMessage(Message message) {
        if (isMessagePersistable(message)) {
            messageRepository.save(message);
        }
    }

    public void updateMessageState(Message message, MessageState state) {
        if (isMessagePersistable(message)) {
            messageRepository.updateState(message.getId(), state);
        }
    }

    @Override
    public void doPreprocess(Message message, T data, MessageContext context) {
        persistMessage(message);
    }
}
