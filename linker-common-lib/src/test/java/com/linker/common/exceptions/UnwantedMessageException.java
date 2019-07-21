package com.linker.common.exceptions;

public class UnwantedMessageException extends Exception {
    public UnwantedMessageException() {
    }

    public UnwantedMessageException(String s) {
        super(s);
    }

    public UnwantedMessageException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnwantedMessageException(Throwable throwable) {
        super(throwable);
    }

    public UnwantedMessageException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
