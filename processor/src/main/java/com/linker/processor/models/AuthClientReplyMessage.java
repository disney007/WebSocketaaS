package com.linker.processor.models;

import com.linker.common.MessageResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthClientReplyMessage {
    MessageResult result;
}
