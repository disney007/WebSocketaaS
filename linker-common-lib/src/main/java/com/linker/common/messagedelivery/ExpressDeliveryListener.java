package com.linker.common.messagedelivery;

public interface ExpressDeliveryListener {
    void onMessageArrived(ExpressDelivery expressDelivery, byte[] message);

    default void onMessageDelivered(ExpressDelivery expressDelivery, String target, byte[] message) {
    }

    void onMessageDeliveryFailed(ExpressDelivery expressDelivery, byte[] message);
}
