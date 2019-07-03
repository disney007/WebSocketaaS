package com.linker.connector.express;

import com.linker.common.messagedelivery.NatsExpressDelivery;

import java.io.IOException;

public class MockNatsExpressDelivery extends NatsExpressDelivery {

    public MockNatsExpressDelivery() {
        super(null, null);
    }

    @Override
    public void deliveryMessage(String target, String message) throws IOException {
        if (getListener() != null) {
            getListener().onMessageDelivered(this, target, message);
        }
    }

    @Override
    public void onMessageArrived(String message) {
        if (getListener() != null) {
            getListener().onMessageArrived(this, message);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
