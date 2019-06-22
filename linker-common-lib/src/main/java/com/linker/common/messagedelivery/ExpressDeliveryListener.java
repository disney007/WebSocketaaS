package com.linker.common.messagedelivery;

public interface ExpressDeliveryListener {
    void onMessageArrived(ExpressDelivery expressDelivery, String message);
}
