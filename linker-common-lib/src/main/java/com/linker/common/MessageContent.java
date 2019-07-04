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

    public <T> T getData(Class<T> clazz) {
        return Utils.convert(data, clazz);
    }
}
