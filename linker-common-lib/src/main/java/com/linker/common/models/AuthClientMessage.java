package com.linker.common.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class AuthClientMessage {
    String appId;
    String userId;
    String token;
}
