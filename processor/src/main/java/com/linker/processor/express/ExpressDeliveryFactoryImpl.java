package com.linker.processor.express;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.KafkaCache;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.processor.configurations.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpressDeliveryFactoryImpl implements ExpressDeliveryFactory {

    @Autowired
    ApplicationConfig applicationConfig;


    public ExpressDelivery createKafkaExpressDelivery(KafkaCache kafkaCache) {
        final String consumerTopics = applicationConfig.getConsumerTopics();
        KafkaExpressDelivery kafka = new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, "group-incoming");
        kafka.setKafkaCache(kafkaCache);
        return kafka;
    }

    public ExpressDelivery createNatsExpressDelivery() {
        final String consumerTopics = applicationConfig.getConsumerTopics();
        return new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics);
    }
}
