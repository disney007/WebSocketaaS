package com.linker.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;

@Slf4j
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
            default:
                return ExpressDeliveryType.KAFKA;
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.info("thread interrupted during sleep, ignore");
        }
    }

    public static <T> T getRandomItemInCollection(Collection<T> collection) {
        if (collection == null || collection.size() == 0) {
            return null;
        }

        return collection.stream()
                .skip(new Random().nextInt(collection.size()))
                .findAny()
                .get();
    }

    public static <T> T defaultValue(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
