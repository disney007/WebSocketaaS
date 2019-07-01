package com.linker.common.models;

import com.linker.common.MessageSnapshot;
import com.linker.common.MessageState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStateChangedMessage {
    MessageSnapshot message;
    MessageState state;
}
