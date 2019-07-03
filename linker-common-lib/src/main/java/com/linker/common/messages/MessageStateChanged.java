package com.linker.common.messages;

import com.linker.common.MessageSnapshot;
import com.linker.common.MessageState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MessageStateChanged {
    MessageSnapshot message;
    MessageState state;
}
