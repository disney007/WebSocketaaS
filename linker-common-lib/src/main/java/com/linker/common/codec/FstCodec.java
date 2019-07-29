package com.linker.common.codec;


import org.nustaq.serialization.FSTConfiguration;

public class FstCodec implements Codec {
    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Override
    public <T> byte[] serialize(T obj) {
        return conf.asByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) conf.asObject(data);
    }
}
