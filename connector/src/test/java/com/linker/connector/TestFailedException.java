package com.linker.connector;

public class TestFailedException extends RuntimeException {

    public TestFailedException() {
    }

    public TestFailedException(String message) {
        super(message);
    }

    public TestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestFailedException(Throwable cause) {
        super(cause);
    }

    public TestFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
