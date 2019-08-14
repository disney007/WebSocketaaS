package com.linker.processor.services;

import com.linker.common.messagedelivery.KafkaCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class KafkaCacheService implements KafkaCache {

    final static String CACHE_NAME = "KAFKA_CACHE";

    final RedisTemplate<String, String> redisTemplate;


    @Override
    public void addItem(String item) {
        redisTemplate.opsForSet().add(CACHE_NAME, item);
    }

    @Override
    public Set<String> getDuplicateItems(List<String> items) {
        return redisTemplate.opsForSet().intersect(CACHE_NAME, items);
    }

    @Override
    public void deleteItems(List<String> items) {
        Object[] objs = items.toArray(new String[0]);
        redisTemplate.opsForSet().remove(CACHE_NAME, objs);
    }
}
