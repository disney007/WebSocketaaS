package com.linker.processor.messagedelivery;

import com.linker.common.express.ExpressDeliveryType;
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
    Connection connection;
    Channel channel;

    @Autowired
    PostOffice postOffice;

    static class IncomingMessageConsumer extends DefaultConsumer {

        RabbitMQExpressDelivery expressDelivery;

        IncomingMessageConsumer(RabbitMQExpressDelivery expressDelivery, Channel channel) {
            super(channel);
            this.expressDelivery = expressDelivery;
        }

        @Override
        public void handleDelivery(
                String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body) throws IOException {

            String message = new String(body, "UTF-8");
            this.expressDelivery.onMessageArrived(message);
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

            Consumer consumer = new IncomingMessageConsumer(this, channel);
            channel.basicConsume("message_incoming_queue", true, consumer);
            log.info("rabbitmq:connected to message queue");
        } catch (IOException | TimeoutException e) {
            log.error("rabbitmq:failed to connect message queue", e);
        }
    }


    public void onMessageArrived(String message) {
        postOffice.onMessageArrived(message, this);
    }

    public void deliveryMessage(String message) throws IOException {
        channel.basicPublish("", "message_outgoing_queue", null, message.getBytes());
    }

    @PreDestroy
    void onDestroy() {
        log.info("rabbitmq:deregister from message queue");
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                log.error("rabbitmq:close message channel failed", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                log.error("rabbitmq:close message connection failed", e);
            }
        }
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.RABBITMQ;
    }
}
