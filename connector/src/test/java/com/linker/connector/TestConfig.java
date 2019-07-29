package com.linker.connector;

import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.spy;

@Configuration
public class TestConfig {

    @Autowired
    IntegrationTestEnv integrationTestEnv;

    @Autowired
    Codec codec;

    @Bean
    public MockKafkaExpressDelivery kafkaExpressDelivery() {
        return spy(new MockKafkaExpressDelivery(codec));
    }

    @Bean
    public MockNatsExpressDelivery natsExpressDelivery() {
        return spy(new MockNatsExpressDelivery(codec));
    }
}
