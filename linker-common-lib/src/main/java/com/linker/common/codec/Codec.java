package com.linker.common.codec;

public interface Codec {
    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] data, Class<T> clazz);
}
