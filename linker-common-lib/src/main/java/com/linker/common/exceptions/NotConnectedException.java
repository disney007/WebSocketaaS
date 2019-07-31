package com.linker.common.exceptions;

public class NotConnectedException extends Exception {
    public NotConnectedException() {
    }

    public NotConnectedException(String s) {
        super(s);
    }

    public NotConnectedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NotConnectedException(Throwable throwable) {
        super(throwable);
    }

    public NotConnectedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
