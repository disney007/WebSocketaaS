package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    String messageId = UUID.randomUUID().toString();
    String version = "0.1.0";
    MessageContent content;
    String from;
    String to;
    MessageMeta meta;
    long createdAt = ZonedDateTime.now().toEpochSecond();
    MessageState state = MessageState.CREATED;

    @Override
    public String toString() {
        return String.format("[version=%s, messageId=%s, type=%s, from=%s, to=%s, createdAt=%d, state=%s]",
                version, messageId, (content != null ? content.getType() : "unknown"), from, to, createdAt, state);
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        Message message;

        public MessageBuilder() {
            message = new Message();
        }

        public MessageBuilder messageId(String messageId) {
            message.setMessageId(messageId);
            return this;
        }

        public MessageBuilder content(MessageContent content) {
            message.setContent(content);
            return this;
        }

        public MessageBuilder from(String from) {
            message.setFrom(from);
            return this;
        }

        public MessageBuilder to(String to) {
            message.setTo(to);
            return this;
        }

        public MessageBuilder meta(MessageMeta meta) {
            message.setMeta(meta);
            return this;
        }

        public MessageBuilder createdAt(long timestamp) {
            message.setCreatedAt(timestamp);
            return this;
        }

        public MessageBuilder state(MessageState state) {
            message.setState(state);
            return this;
        }

        public Message build() {
            return message;
        }
    }
}
