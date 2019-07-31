package com.linker.common.exceptions;

public class RouterNotConnectedException extends Exception {
    public RouterNotConnectedException() {
    }

    public RouterNotConnectedException(String s) {
        super(s);
    }

    public RouterNotConnectedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RouterNotConnectedException(Throwable throwable) {
        super(throwable);
    }

    public RouterNotConnectedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
