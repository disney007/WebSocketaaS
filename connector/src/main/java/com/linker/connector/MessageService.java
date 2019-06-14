package com.linker.connector;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class MessageService {
    Channel channel;
    Connection connection;

    public MessageService() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("message_incoming_queue", false, false, false, null);
            channel.queueDeclare("message_outgoing_queue", false, false, false, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(
                        String consumerTag,
                        Envelope envelope,
                        AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    String message = new String(body, "UTF-8");
                    log.info("received message from outgoing queue {}", message);
                    WebSocketHandler.sendMessage(message);
                }
            };
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

    public void sendMessage(String message) throws IOException {
        log.debug("send message:{}", message);
        channel.basicPublish("", "message_incoming_queue", null, message.getBytes());
    }
}
