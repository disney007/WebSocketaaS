package com.linker.processor.express;

import com.linker.common.messagedelivery.*;
import com.linker.processor.configurations.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpressDeliveryFactoryImpl implements ExpressDeliveryFactory {

    final ApplicationConfig applicationConfig;

    final RedisTemplate<String, String> redisTemplate;


    public ExpressDelivery createKafkaExpressDelivery() {
        final String consumerTopics = applicationConfig.getConsumerTopics();
        KafkaExpressDelivery kafka = new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, "group-incoming");
        kafka.setKafkaCache(new RedisKafkaCache(redisTemplate));
        return kafka;
    }

    public ExpressDelivery createNatsExpressDelivery() {
        final String consumerTopics = applicationConfig.getConsumerTopics();
        return new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics);
    }
}
