package com.linker.connector;

public interface SocketHandler {
    void sendMessage(String message);

    void setUserId(String userId);

    String getUserId();
}
