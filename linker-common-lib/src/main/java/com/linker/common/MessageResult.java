package com.linker.common;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class MessageResult {
    ResultStatus status;
    String message;

    public MessageResult(ResultStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public MessageResult(ResultStatus status) {
        this.status = status;
    }

    public static MessageResult ok() {
        return new MessageResult(ResultStatus.OK);
    }
}
