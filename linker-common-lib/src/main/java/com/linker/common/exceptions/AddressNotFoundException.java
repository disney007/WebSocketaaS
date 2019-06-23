package com.linker.common.exceptions;

import com.linker.common.Message;

public class AddressNotFoundException extends RuntimeException {
    Message message;

    public AddressNotFoundException(Message message) {
        this.message = message;
    }
}
