package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Document(collection = "message")
public class Message implements Serializable {

    private static final long serialVersionUID = -5109122406309247512L;

    @Id
    String id = UUID.randomUUID().toString();
    String version = "0.1.0";
    MessageContent content;
    String from;
    @Indexed
    String to;
    MessageMeta meta;
    long createdAt = ZonedDateTime.now().toInstant().toEpochMilli();
    @Indexed
    MessageState state = MessageState.CREATED;

    @Override
    public String toString() {
        return String.format("[version=%s, id=%s, type=%s, from=%s, to=%s, createdAt=%d, state=%s]",
                version, id, (content != null ? content.getType() : "unknown"), from, to, createdAt, state);
    }

    public Message clone() {
        return builder().id(id)
                .version(version)
                .content(new MessageContent(content.getType(), content.getData(), content.getReference(), content.getFeature(), content.getConfirmationEnabled()))
                .from(from)
                .to(to)
                .meta(new MessageMeta(meta.getOriginalAddress(), meta.getTargetAddress(), meta.getNote(), meta.getTtl()))
                .createdAt(createdAt)
                .state(state)
                .build();
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        Message message;

        public MessageBuilder() {
            message = new Message();
        }

        public MessageBuilder id(String id) {
            message.setId(id);
            return this;
        }

        public MessageBuilder version(String version) {
            message.setVersion(version);
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
