package com.linker.common.messages;

import com.linker.common.MessageResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AuthClientReply extends AuthClient {
    MessageResult result;
}
