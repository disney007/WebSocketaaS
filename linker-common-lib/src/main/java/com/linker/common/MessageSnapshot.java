package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MessageSnapshot {
    String id;
    String version;
    String from;
    String to;
    MessageState state;
    MessageType type;
    MessageFeature feature;
    long createdAt;
}
