package com.linker.common.messages;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AuthClient implements Serializable {

    private static final long serialVersionUID = -2476945184181905355L;
    String appId;
    String userId;
    String token;
}
