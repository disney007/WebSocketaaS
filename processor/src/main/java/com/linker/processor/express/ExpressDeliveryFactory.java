package com.linker.processor.express;

import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.KafkaCache;

public interface ExpressDeliveryFactory {
    ExpressDelivery createKafkaExpressDelivery();

    ExpressDelivery createNatsExpressDelivery();
}
