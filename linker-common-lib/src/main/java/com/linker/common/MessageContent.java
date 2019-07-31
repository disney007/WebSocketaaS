package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MessageContent implements Serializable {
    private static final long serialVersionUID = -4589317958936125545L;
    @Indexed
    MessageType type;
    Object data;
    String reference;
    MessageFeature feature = MessageFeature.RELIABLE;
    Boolean confirmationEnabled = true;

    public MessageContentOutput toContentOutput() {
        return new MessageContentOutput(type, data, reference);
    }
}
