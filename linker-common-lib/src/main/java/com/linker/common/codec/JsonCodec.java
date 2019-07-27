package com.linker.common.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Utils;
import org.apache.commons.lang3.SerializationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonCodec implements Codec {
    @Override
    public <T> byte[] serialize(T obj) {
        try {
            return Utils.toJson(obj).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            return Utils.fromJson(new String(data, StandardCharsets.UTF_8), clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
