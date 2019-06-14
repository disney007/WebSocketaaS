package com.linker.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
    String messageId;
    String appId;
    String from;
    String to;
    String data;
    long clientCreatedTimestamp;
    long serverCreatedTimestamp;
    MessageState state;
}
