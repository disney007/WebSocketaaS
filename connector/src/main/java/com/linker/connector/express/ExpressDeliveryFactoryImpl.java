package com.linker.connector.express;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.common.messagedelivery.RedisKafkaCache;
import com.linker.connector.configurations.ApplicationConfig;
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
        final String connectorName = applicationConfig.getConnectorName();
        final String consumerTopics = connectorName;
        KafkaExpressDelivery kafka = new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, connectorName);
        kafka.setKafkaCache(new RedisKafkaCache(redisTemplate));
        return kafka;
    }

    public ExpressDelivery createNatsExpressDelivery() {
        final String consumerTopics = applicationConfig.getConnectorName();
        return new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics);
    }
}
