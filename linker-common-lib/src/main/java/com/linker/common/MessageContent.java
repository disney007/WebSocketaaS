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
public class MessageContent {
    MessageType type;
    Object data;
    String reference;
    MessageFeature feature = MessageFeature.RELIABLE;
    Boolean confirmationEnabled = true;

    public MessageContentOutput toContentOutput() {
        return new MessageContentOutput(type, data, reference);
    }
}
