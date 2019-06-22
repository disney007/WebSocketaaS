package com.linker.connector.messagedelivery;

import com.linker.common.express.ExpressDeliveryType;
import com.linker.connector.PostOffice;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class NatsExpressDelivery implements ExpressDelivery {
    private static String OUTGOING_TOPIC = "outgoing-topic";
    private static String INCOMING_TOPIC = "incoming-topic";

    Connection connection;

    @Autowired
    PostOffice postOffice;

    @PostConstruct
    public void setup() {
        try {
            Options o = new Options.Builder().server("nats://localhost:4222").build();
            connection = Nats.connect(o);
            Dispatcher dispatcher = connection.createDispatcher(message -> {
                String msg = new String(message.getData(), StandardCharsets.UTF_8);
                this.onMessageArrived(msg);
            });
            dispatcher.subscribe(OUTGOING_TOPIC);
            log.info("Nats:connected");
        } catch (IOException | InterruptedException e) {
            log.error("Nats:failed to start", e);
        }
    }

    @PreDestroy
    public void onDestroy() {
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
    public void deliveryMessage(String message) throws IOException {
        connection.publish(INCOMING_TOPIC, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onMessageArrived(String message) {
        postOffice.onMessageArrived(message, this);
    }
}
