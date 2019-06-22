package com.linker.common.messagedelivery;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


@Slf4j
public class RabbitMQExpressDelivery implements ExpressDelivery {
    Channel channel;
    Connection connection;

    @Setter
    ExpressDeliveryListener listener;

    String hosts;
    String consumerQueue;

    public RabbitMQExpressDelivery(String hosts, String consumerQueue) {
        this.hosts = hosts;
        this.consumerQueue = consumerQueue;
    }

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


    @Override
    public void start() {
        String[] strs = this.hosts.split(":");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(strs[0]);
        factory.setPort(Integer.parseInt(strs[1]));
        factory.setAutomaticRecoveryEnabled(true);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(consumerQueue, false, false, false, null);
            Consumer consumer = new OutgoingMessageConsumer(this, channel);
            channel.basicConsume(consumerQueue, true, consumer);
            log.info("connected to message queue");
        } catch (IOException | TimeoutException e) {
            log.error("connect to message queue failed", e);
        }
    }

    @Override
    public void stop() {
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
    public void deliveryMessage(String target, String message) throws IOException {
        channel.queueDeclare(target, false, false, false, null);
        channel.basicPublish("", target, null, message.getBytes());
    }

    @Override
    public void onMessageArrived(String message) {
        listener.onMessageArrived(this, message);
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.RABBITMQ;
    }
}
