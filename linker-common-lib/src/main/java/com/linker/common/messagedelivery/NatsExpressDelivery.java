package com.linker.common.messagedelivery;

import com.linker.common.Utils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


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
                String msg = new String(message.getData(), StandardCharsets.UTF_8);
                this.onMessageArrived(msg);
            });
            consumerDispatcher.subscribe(consumerTopic);
            log.info("Nats:connected");
        } catch (IOException | InterruptedException e) {
            log.error("Nats:failed to start", e);
        }
    }

    @Override
    public void stop() {
        log.info("Nats: close consumer");
        if (consumerDispatcher != null) {
            consumerDispatcher.unsubscribe(consumerTopic);
        }
        Utils.sleep(3000L);
        log.info("Nats: close connection");
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
        if (listener != null) {
            listener.onMessageDelivered(this, target, message);
        }
    }

    @Override
    public void onMessageArrived(String message) {
        if (listener != null) {
            listener.onMessageArrived(this, message);
        }
    }
}
