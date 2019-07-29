package com.linker.common.messagedelivery;

import java.io.IOException;

public interface ExpressDelivery {

    void setListener(ExpressDeliveryListener listener);
    ExpressDeliveryType getType();

    void deliveryMessage(String target, byte[] message) throws IOException;

    void onMessageArrived(byte[] message);

    void start();
    void stop();
}
