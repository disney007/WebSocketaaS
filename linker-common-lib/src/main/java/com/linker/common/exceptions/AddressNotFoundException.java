package com.linker.common.exceptions;

import com.linker.common.Message;

public class AddressNotFoundException extends RuntimeException {
    Message message;

    public AddressNotFoundException(Message message) {
        super(message.toString());
        this.message = message;
    }

    public AddressNotFoundException(Message message, Throwable throwable) {
        super(message.toString(), throwable);
        this.message = message;
    }
}
