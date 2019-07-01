package com.linker.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageContent {
    MessageType type;
    Object data;
    String reference;
    MessageFeature feature = MessageFeature.NORMAL;
}
