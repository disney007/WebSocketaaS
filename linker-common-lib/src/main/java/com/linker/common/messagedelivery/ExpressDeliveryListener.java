package com.linker.common.messagedelivery;

public interface ExpressDeliveryListener {
    void onMessageArrived(ExpressDelivery expressDelivery, String message);

    default void onMessageDelivered(ExpressDelivery expressDelivery, String target, String message) {
    }
}
