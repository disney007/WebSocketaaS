package com.linker.connector;

import com.linker.common.Message;
import com.linker.common.Utils;
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

    static class OutgoingMesssageConsumer extends DefaultConsumer {
        MessageService messageService;

        public OutgoingMesssageConsumer(MessageService messageService, Channel channel) {
            super(channel);
            this.messageService = messageService;
        }

        @Override
        public void handleDelivery(
                String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body) throws IOException {


            Message message = Utils.fromJson(new String(body, "UTF-8"), Message.class);
            log.info("received message from outgoing queue {}", message);
            this.messageService.onMessageReceived(message);
        }
    }

    public MessageService() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("message_incoming_queue", false, false, false, null);
            channel.queueDeclare("message_outgoing_queue", false, false, false, null);

            Consumer consumer = new OutgoingMesssageConsumer(this, channel);
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

    public void sendMessage(Message message) throws IOException {
        log.info("send message:{}", message);
        String msg = Utils.toJson(message);
        channel.basicPublish("", "message_incoming_queue", null, msg.getBytes());
    }

    public void onMessageReceived(Message message) throws IOException {
        String content = Utils.toJson(message.getContent());
        WebSocketHandler.sendMessage(content);
    }
}
