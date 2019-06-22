package com.linker.common.messagedelivery;

import java.io.IOException;

public interface ExpressDelivery {

    void setListener(ExpressDeliveryListener listener);
    ExpressDeliveryType getType();

    void deliveryMessage(String target, String message) throws IOException;

    void onMessageArrived(String message);

    void start();
    void stop();
}
