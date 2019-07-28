package com.linker.common.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AuthClientReply implements Serializable {

    private static final long serialVersionUID = -6696238110393994234L;
    String appId;
    String userId;
    Boolean isAuthenticated;
}
