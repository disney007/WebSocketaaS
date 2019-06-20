package com.linker.connector;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class NetworkUserService {
    ConcurrentHashMap<String, SocketHandler> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, SocketHandler> pendingUsers = new ConcurrentHashMap<>();

    public void addUser(String userId, SocketHandler socketHandler) {
        users.put(userId, socketHandler);
    }

    public SocketHandler removeUser(String userId) {
        return users.remove(userId);
    }

    public SocketHandler getUser(String userId) {
        return users.getOrDefault(userId, null);
    }

    public void addPendingUser(String userId, SocketHandler socketHandler) {
        pendingUsers.put(userId, socketHandler);
    }

    public SocketHandler removePendingUser(String userId) {
        return pendingUsers.remove(userId);
    }
}
