package com.linker.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.linker.common.messagedelivery.ExpressDeliveryType;

import java.io.IOException;

public class Utils {
    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        return objectMapper.convertValue(object, clazz);
    }

    public static ExpressDeliveryType calcExpressDelivery(MessageFeature feature) {
        switch (feature) {
            case FAST:
                return ExpressDeliveryType.NATS;
            case RELIABLE:
                return ExpressDeliveryType.RABBITMQ;
            default:
                return ExpressDeliveryType.KAFKA;
        }
    }
}
