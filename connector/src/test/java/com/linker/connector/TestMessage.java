package com.linker.connector;

import com.linker.common.MessageFeature;
import com.linker.common.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TestMessage {
    MessageType type;
    Map<String, Object> data;
    MessageFeature feature;
}
