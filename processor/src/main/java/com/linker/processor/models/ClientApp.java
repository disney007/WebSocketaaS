package com.linker.processor.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("client-app")
public class ClientApp {
    @Id
    String appName; // app name as id is for fast access from userId
    String appId;
    String masterUserId;
    String authUrl;
    boolean authEnabled;
}
