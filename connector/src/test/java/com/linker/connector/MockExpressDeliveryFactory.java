package com.linker.connector;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.connector.express.ExpressDeliveryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static org.mockito.Mockito.when;

@Service
@Primary
public class MockExpressDeliveryFactory implements ExpressDeliveryFactory {

    @Autowired
    KafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    NatsExpressDelivery natsExpressDelivery;

    public ExpressDelivery createKafkaExpressDelivery() {
        when(kafkaExpressDelivery.getType()).thenReturn(ExpressDeliveryType.KAFKA);
        return kafkaExpressDelivery;
    }

    public ExpressDelivery createNatsExpressDelivery() {
        when(natsExpressDelivery.getType()).thenReturn(ExpressDeliveryType.NATS);
        return natsExpressDelivery;
    }
}
