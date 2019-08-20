package com.linker.common.messagedelivery;

public interface ExpressDelivery {

    void setListener(ExpressDeliveryListener listener);

    ExpressDeliveryType getType();

    void deliverMessage(String target, byte[] message);

    void onMessageArrived(byte[] message);

    void start();

    void stopConsumer();
    void stopProducer();
}
