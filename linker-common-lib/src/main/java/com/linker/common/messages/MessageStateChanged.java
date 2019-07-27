package com.linker.common.messages;

import com.linker.common.Message;
import com.linker.common.MessageState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MessageStateChanged implements Serializable {
    Message message;
    MessageState state;
}
