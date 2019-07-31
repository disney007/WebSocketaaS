package com.linker.processor.express;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.KafkaCache;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.processor.express.ExpressDeliveryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MockExpressDeliveryFactory implements ExpressDeliveryFactory {


    @Autowired
    KafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    NatsExpressDelivery natsExpressDelivery;

    public ExpressDelivery createKafkaExpressDelivery(KafkaCache kafkaCache) {
        return kafkaExpressDelivery;
    }

    public ExpressDelivery createNatsExpressDelivery() {
        return natsExpressDelivery;
    }
}
