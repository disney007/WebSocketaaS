package com.linker.common.models;

import com.linker.common.MessageResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthClientReplyMessage extends AuthClientMessage{
    MessageResult result;
}
