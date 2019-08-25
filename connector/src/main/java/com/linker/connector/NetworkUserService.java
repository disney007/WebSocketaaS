package com.linker.connector;

import com.google.common.collect.ImmutableList;
import com.linker.connector.network.SocketHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NetworkUserService {
    ConcurrentHashMap<String, List<SocketHandler>> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, List<SocketHandler>> pendingUsers = new ConcurrentHashMap<>();

    public void addUser(String userId, SocketHandler socketHandler) {
        addUser(users, userId, socketHandler);
    }


    public void removeUser(String userId, Long socketId) {
        removeUser(users, userId, socketId);
    }


    public List<SocketHandler> getUser(String userId) {
        return users.getOrDefault(userId, ImmutableList.of());
    }

    public SocketHandler getUser(String userId, Long socketId) {
        return getUser(users, userId, socketId);
    }

    public void addPendingUser(String userId, SocketHandler socketHandler) {
        addUser(pendingUsers, userId, socketHandler);
    }

    public void removePendingUser(String userId, Long socketId) {
        removeUser(pendingUsers, userId, socketId);
    }

    public List<SocketHandler> getPendingUser(String userId) {
        return pendingUsers.getOrDefault(userId, ImmutableList.of());
    }

    public SocketHandler getPendingUser(String userId, Long socketId) {
        return getUser(pendingUsers, userId, socketId);
    }

    static void addUser(ConcurrentHashMap<String, List<SocketHandler>> map, String userId, SocketHandler socketHandler) {
        if (!map.containsKey(userId)) {
            map.put(userId, new ArrayList<>());
        }
        map.get(userId).add(socketHandler);
    }

    static void removeUser(ConcurrentHashMap<String, List<SocketHandler>> map, String userId, Long socketId) {
        List<SocketHandler> socketHandlers = map.get(userId);
        if (socketHandlers != null) {
            socketHandlers.removeIf(s -> s == null || s.getSocketId().equals(socketId));
            if (socketHandlers.isEmpty()) {
                map.remove(userId);
            }
        }
    }

    static SocketHandler getUser(ConcurrentHashMap<String, List<SocketHandler>> map, String userId, Long socketId) {
        List<SocketHandler> handlers = map.getOrDefault(userId, null);
        if (handlers != null) {
            Optional<SocketHandler> op = handlers.stream().filter(s -> s.getSocketId().equals(socketId)).findFirst();
            if (op.isPresent()) {
                return op.get();
            }
        }
        return null;
    }


}
