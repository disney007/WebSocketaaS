package com.linker.common;

import com.google.common.reflect.TypeToken;
import com.linker.common.exceptions.ProcessMessageException;

import java.io.IOException;

public abstract class MessageProcessor<T> {

    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };

    public MessageProcessor() {

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
