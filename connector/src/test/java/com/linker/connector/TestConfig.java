package com.linker.connector;

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

    @Bean
    public MockKafkaExpressDelivery kafkaExpressDelivery() {
        return spy(new MockKafkaExpressDelivery());
    }

    @Bean
    public MockNatsExpressDelivery natsExpressDelivery() {
        return spy(new MockNatsExpressDelivery());
    }
}
