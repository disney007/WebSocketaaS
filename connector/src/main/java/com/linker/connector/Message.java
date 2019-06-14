package com.linker.connector;

import lombok.Getter;
import lombok.ToString;

public class Message {
    public enum ClientMessageType {
        DATA,
        CLOSE_ON_MAINTENANCE,
        CLOSE_ON_UNAUTHORIZED
    }

    @Getter
    @ToString
    public static class ClientMessageRequest {
        String to;
        String data;
        long timestamp;
    }

    public static class ClientMessageResponse {
        ClientMessageType type;
        String from;
        String data;
        long timestamp;
    }
}
