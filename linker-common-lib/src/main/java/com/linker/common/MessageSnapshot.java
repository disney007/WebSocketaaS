package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MessageSnapshot {
    String id;
    String version;
    String from;
    String to;
    MessageState state;
    MessageType type;
    MessageFeature feature;
    String reference;
    long createdAt;

    public Message toMessage() {
        return Message.builder()
                .id(id)
                .version(version)
                .from(from)
                .to(to)
                .state(state)
                .content(MessageUtils.createMessageContent(type, null, reference, feature))
                .createdAt(createdAt)
                .build();
    }
}
