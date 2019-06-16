package com.linker.common;

import lombok.Getter;

@Getter
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
