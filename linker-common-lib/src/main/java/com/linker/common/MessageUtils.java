package com.linker.common;


public class MessageUtils {
    public static MessageContent createMessageContent(MessageType type, Object data, String reference, MessageFeature feature) {
        return new MessageContent(type.toString(), data, reference, feature);
    }

    public static MessageContent createMessageContent(MessageType type, Object data, MessageFeature feature) {
        return new MessageContent(type.toString(), data, null, feature);
    }
}
