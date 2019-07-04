package com.linker.connector.express;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MockKafkaExpressDelivery extends KafkaExpressDelivery {

    LinkedBlockingQueue<Message> deliveredMessageQueue = new LinkedBlockingQueue<>();

    public MockKafkaExpressDelivery() {
        super(null, null, null);
    }

    @Override
    public void deliveryMessage(String target, String message) throws IOException {
        if (getListener() != null) {
            getListener().onMessageDelivered(this, target, message);
        }
        deliveredMessageQueue.add(Utils.fromJson(message, Message.class));
    }

    @Override
    public void onMessageArrived(String message) {
        if (getListener() != null) {
            getListener().onMessageArrived(this, message);
        }
    }

    public void reset() {
        deliveredMessageQueue.clear();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public Message getDeliveredMessage() throws InterruptedException {
        return deliveredMessageQueue.poll(3L, TimeUnit.SECONDS);
    }

    public Message getDeliveredMessage(MessageType type) throws InterruptedException, TimeoutException {
        log.info("kafka:wanting for message {}", type);
        Message message;
        do {
            message = getDeliveredMessage();
            if (message == null) {
                throw new TimeoutException("kafka:failed to get message " + type);
            }
        } while (message.getContent().getType() != type);
        return message;
    }
}