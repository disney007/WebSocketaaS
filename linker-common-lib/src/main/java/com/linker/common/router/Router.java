package com.linker.common.router;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Set;

@Data
@RedisHash("router")
public class Router {
    @Id
    String name;
    String url;
    Set<String> domains;

    @Override
    public String toString() {
        return String.format("[%s %s]", this.name, this.url);
    }
}
