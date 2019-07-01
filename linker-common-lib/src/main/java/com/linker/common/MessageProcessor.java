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
        try {
            T data = getData(message);
            doProcess(message, data, context);
        } catch (IOException e) {
            throw new ProcessMessageException(e);
        }
    }

    public void preprocess(Message message, MessageContext context){
        T data = getData(message);
        doPreprocess(message, data, context);
    }

    T getData(Message message) {
        typeToken.getRawType();
        return (T) Utils.convert(message.getContent().getData(), typeToken.getRawType());
    }

    public abstract MessageType getMessageType();

    public void doPreprocess(Message message, T data, MessageContext context) {

    }

    public abstract void doProcess(Message message, T data, MessageContext context) throws IOException;
}
