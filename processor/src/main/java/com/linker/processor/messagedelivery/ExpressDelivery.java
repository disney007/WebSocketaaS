package com.linker.processor.messagedelivery;

import com.linker.common.express.ExpressDeliveryType;

import java.io.IOException;

public interface ExpressDelivery {
    ExpressDeliveryType getType();

    void deliveryMessage(String message) throws IOException;

    void onMessageArrived(String message);
}
