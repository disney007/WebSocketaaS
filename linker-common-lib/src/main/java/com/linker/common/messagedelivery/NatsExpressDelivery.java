package com.linker.common.messagedelivery;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Slf4j
public class NatsExpressDelivery implements ExpressDelivery {
    Connection connection;

    @Setter
    ExpressDeliveryListener listener;

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
            Dispatcher dispatcher = connection.createDispatcher(message -> {
                String msg = new String(message.getData(), StandardCharsets.UTF_8);
                this.onMessageArrived(msg);
            });
            dispatcher.subscribe(consumerTopic);
            log.info("Nats:connected");
        } catch (IOException | InterruptedException e) {
            log.error("Nats:failed to start", e);
        }
    }

    @Override
    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (InterruptedException e) {
                log.error("Nats:failed to close", e);
            }
        }
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.NATS;
    }

    @Override
    public void deliveryMessage(String target, String message) throws IOException {
        connection.publish(target, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onMessageArrived(String message) {
        listener.onMessageArrived(this, message);
    }
}
