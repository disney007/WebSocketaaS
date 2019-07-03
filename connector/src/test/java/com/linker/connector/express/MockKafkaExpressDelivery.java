package com.linker.connector.express;

import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.connector.TestFailedException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockKafkaExpressDelivery extends KafkaExpressDelivery {

    CompletableFuture<Message> deliveryMessageFuture;

    public MockKafkaExpressDelivery() {
        super(null, null, null);
    }

    @Override
    public void deliveryMessage(String target, String message) throws IOException {
        if (getListener() != null) {
            getListener().onMessageDelivered(this, target, message);
        }
        deliveryMessageFuture.complete(Utils.fromJson(message, Message.class));
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

    public Message getDeliveredMessage() {
        deliveryMessageFuture = new CompletableFuture<>();
        try {
            return deliveryMessageFuture.get(3000L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TestFailedException(e);
        }
    }
}