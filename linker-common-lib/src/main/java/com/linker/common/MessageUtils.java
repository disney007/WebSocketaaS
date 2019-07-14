package com.linker.common;


public class MessageUtils {
    public static MessageContent createMessageContent(MessageType type, Object data, String reference, MessageFeature feature) {
        return new MessageContent(type, data, reference, feature, true);
    }

    public static MessageContent createMessageContent(MessageType type, Object data, MessageFeature feature) {
        return new MessageContent(type, data, null, feature, true);
    }


    public static boolean isMessageAlive(Message message) {
        Integer ttl = message.getMeta().getTtl();
        if (ttl == null) {
            ttl = 10;
        }
        return ttl > 0;
    }

    public static Message touchMessage(Message message, int count) {
        Integer ttl = message.getMeta().getTtl();
        if (ttl == null) {
            ttl = 10;
        }
        message.getMeta().setTtl(ttl - count);
        return message;
    }

    public static Message touchMessage(Message message) {
        return touchMessage(message, 1);
    }
}
