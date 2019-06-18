package com.linker.connector;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class NetworkUserService {
    ConcurrentHashMap<String, SocketHandler> userMap = new ConcurrentHashMap<>();


    public void addUser(String userId, SocketHandler socketHandler) {
        userMap.put(userId, socketHandler);
    }

    public void removeUser(String userId) {
        userMap.remove(userId);
    }
}
