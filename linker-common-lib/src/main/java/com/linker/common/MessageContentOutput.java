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
public class MessageContentOutput {
    MessageType type;
    Object data;
    String reference;

    public <T> T getData(Class<T> clazz) {
        return Utils.convert(data, clazz);
    }
}
