package com.linker.connector.messagedelivery;

import com.linker.common.Message;

import java.io.IOException;

public interface ExpressDelivery {
    void deliveryMessage(Message message) throws IOException;

    void onMessageArrived(Message message);
}
