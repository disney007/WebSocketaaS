package com.linker.common.messagedelivery;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class NatsExpressDelivery implements ExpressDelivery {
    Connection connection;

    @Setter
    @Getter
    ExpressDeliveryListener listener;

    Dispatcher consumerDispatcher;

    String hosts;
    String consumerTopic;

    public NatsExpressDelivery(String hosts, String consumerTopic) {
        this.hosts = hosts;
        this.consumerTopic = consumerTopic;
    }

    @Override
    public void start() {
        try {
            Options o = new Options.Builder().server(hosts).build();
            connection = Nats.connect(o);
            consumerDispatcher = connection.createDispatcher(message -> {
                this.onMessageArrived(message.getData());
            });
            consumerDispatcher.subscribe(consumerTopic, consumerTopic);
            log.info("Nats:connected");
        } catch (IOException | InterruptedException e) {
            log.error("Nats:failed to start", e);
        }
    }

    @Override
    public void stopConsumer() {
        log.info("Nats: close consumer");
        if (consumerDispatcher != null) {
            consumerDispatcher.unsubscribe(consumerTopic);
        }
    }

    @Override
    public void stopProducer() {
        log.info("Nats: close connection");
        if (connection != null) {
            try {
                connection.close();
            } catch (InterruptedException e) {
                log.error("Nats: failed to close", e);
            }
        }
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.NATS;
    }

    @Override
    public void deliverMessage(String target, byte[] message) {
        try {
            connection.publish(target, message);
            if (listener != null) {
                listener.onMessageDelivered(this, target, message);
            }
        } catch (Exception e) {
            log.error("Nats: failed to deliver message", e);
            if (listener != null) {
                listener.onMessageDeliveryFailed(this, message);
            }
        }
    }

    @Override
    public void onMessageArrived(byte[] message) {
        if (listener != null) {
            listener.onMessageArrived(this, message);
        }
    }
}
