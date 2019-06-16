package com.linker.connector;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class NetworkUserService {
    ConcurrentHashMap userMap = new ConcurrentHashMap();

}
