package com.linker.common;


public class MessageUtils {
    public static MessageContent createMessageContent(MessageType type, Object data, String reference) {
        return new MessageContent(type.toString(), data, reference);
    }

    public static MessageContent createMessageContent(MessageType type, Object data) {
        return new MessageContent(type.toString(), data, null);
    }
}
