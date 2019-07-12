package com.linker.processor.express;

import com.linker.common.messagedelivery.ExpressDelivery;

public interface ExpressDeliveryFactory {
    ExpressDelivery createKafkaExpressDelivery();

    ExpressDelivery createNatsExpressDelivery();
}
