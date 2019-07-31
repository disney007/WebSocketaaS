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

    private static final long serialVersionUID = -4478806539708167459L;
    Message message;
    MessageState state;
}
