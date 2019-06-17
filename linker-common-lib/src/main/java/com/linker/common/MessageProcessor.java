package com.linker.common;

import com.google.common.reflect.TypeToken;
import com.linker.common.exceptions.ProcessMessageException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class MessageProcessor<T> {

    static Map<MessageType, MessageProcessor> messageProcessors = new HashMap<>();

    public static MessageProcessor<?> getProcessor(MessageType messageType) {
        if (messageProcessors.containsKey(messageType)) {
            return messageProcessors.get(messageType);
        }

        throw new ProcessMessageException("message processor not found for " + messageType);

    }

    public static Map<MessageType, MessageProcessor> getMessageProcessors() {
        return messageProcessors;
    }

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };

    public MessageProcessor() {
        messageProcessors.put(this.getMessageType(), this);
    }

    public void process(Message message, MessageContext context) {
        typeToken.getRawType();

        try {
            T data = (T) Utils.convert(message.getContent().getData(), typeToken.getRawType());
            doProcess(message, data, context);
        } catch (IOException e) {
            throw new ProcessMessageException(e);
        }
    }

    public abstract MessageType getMessageType();

    public abstract void doProcess(Message message, T data, MessageContext context) throws IOException;
}
