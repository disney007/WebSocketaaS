package com.linker.connector.messagedelivery;

import com.linker.common.express.ExpressDeliveryType;
import com.linker.connector.PostOffice;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMQExpressDelivery implements ExpressDelivery {
    Channel channel;
    Connection connection;

    @Autowired
    PostOffice postOffice;

    static class OutgoingMessageConsumer extends DefaultConsumer {
        RabbitMQExpressDelivery expressDelivery;

        public OutgoingMessageConsumer(RabbitMQExpressDelivery expressDelivery, Channel channel) {
            super(channel);
            this.expressDelivery = expressDelivery;
        }

        @Override
        public void handleDelivery(
                String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body) throws IOException {
            this.expressDelivery.onMessageArrived(new String(body, "UTF-8"));
        }
    }

    @PostConstruct
    void connect() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setAutomaticRecoveryEnabled(true);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("message_incoming_queue", false, false, false, null);
            channel.queueDeclare("message_outgoing_queue", false, false, false, null);

            Consumer consumer = new OutgoingMessageConsumer(this, channel);
            channel.basicConsume("message_outgoing_queue", true, consumer);
            log.info("connected to message queue");
        } catch (IOException | TimeoutException e) {
            log.error("connect to message queue failed", e);
        }
    }

    @PreDestroy
    void onDestroy() {
        log.info("deregister from message queue");
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                log.error("close message channel failed", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                log.error("close message connection failed", e);
            }
        }
    }

    @Override
    public void deliveryMessage(String message) throws IOException {
        channel.basicPublish("", "message_incoming_queue", null, message.getBytes());
    }

    @Override
    public void onMessageArrived(String message) {
        postOffice.onMessageArrived(message, this);
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.RABBITMQ;
    }
}
