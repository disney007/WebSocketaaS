package com.linker.common.messagedelivery;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class MockNatsExpressDelivery extends NatsExpressDelivery {

    LinkedBlockingQueue<Message> deliveredMessageQueue = new LinkedBlockingQueue<>();
    Codec codec;

    public MockNatsExpressDelivery(Codec codec) {
        super(null, null);
        this.codec = codec;
    }

    @Override
    public void deliverMessage(String target, byte[] message) throws IOException {
        if (getListener() != null) {
            getListener().onMessageDelivered(this, target, message);
        }
        deliveredMessageQueue.add(codec.deserialize(message, Message.class));
    }

    @Override
    public void onMessageArrived(byte[] message) {
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

    public void reset() {
        deliveredMessageQueue.clear();
    }

    public Message getDeliveredMessage() throws InterruptedException {
        return deliveredMessageQueue.poll(3L, TimeUnit.SECONDS);
    }

    public Message getDeliveredMessage(MessageType type) throws TimeoutException {
        log.info("nats:waiting for message {}", type);
        Message message;
        do {
            try {
                message = getDeliveredMessage();
            } catch (InterruptedException e) {
                log.error("nats:waiting for message {} interrupted", type);
                message = null;
            }

            if (message == null) {
                throw new TimeoutException("nats:failed to get message " + type);
            }
        } while (message.getContent().getType() != type);

        return message;
    }
}
