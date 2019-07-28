package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MessageContentOutput implements Serializable {
    private static final long serialVersionUID = 3432802125815669879L;
    MessageType type;
    Object data;
    String reference;

    public <T> T getData(Class<T> clazz) {
        return Utils.convert(data, clazz);
    }
}
