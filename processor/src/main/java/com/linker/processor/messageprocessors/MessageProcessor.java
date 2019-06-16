package com.linker.processor.messageprocessors;

import com.google.common.reflect.TypeToken;
import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.processor.exceptions.ProcessMessageException;
import com.linker.common.MessageType;

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

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };

    public MessageProcessor() {
        messageProcessors.put(this.getMessageType(), this);
    }

    public void process(Message message) {
        typeToken.getRawType();

        T data = (T) Utils.convert(message.getContent().getData(), typeToken.getRawType());
        doProcess(message, data);
    }

    public abstract MessageType getMessageType();

    public abstract void doProcess(Message message, T data);
}
