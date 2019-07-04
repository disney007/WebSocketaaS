package com.linker.processor.models;

import com.linker.common.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Set;

@RedisHash("user-channel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChannel {
    @Id
    String username;
    Set<Address> addresses;
    Long createdAt;
}
