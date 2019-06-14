package com.linker.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Utils {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}
