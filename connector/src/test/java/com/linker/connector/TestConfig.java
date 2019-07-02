package com.linker.connector;

import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @MockBean
    KafkaExpressDelivery kafkaExpressDelivery;

    @MockBean
    NatsExpressDelivery natsExpressDelivery;
}
