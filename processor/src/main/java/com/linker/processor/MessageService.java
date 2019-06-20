package com.linker.processor;


import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.processor.messageprocessors.MessageProcessorService;
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
public class MessageService {

    Connection connection;
    Channel channel;
    @Autowired
    MessageProcessorService messageProcessor;

    static class IncomingMessageConsumer extends DefaultConsumer {

        MessageService messageService;

        IncomingMessageConsumer(MessageService messageService, Channel channel) {
            super(channel);
            this.messageService = messageService;
        }

        @Override
        public void handleDelivery(
                String consumerTag,
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body) throws IOException {

            String message = new String(body, "UTF-8");
            Message msg = Utils.fromJson(message, Message.class);
            this.messageService.onMessageReceived(msg);
        }
    }


    public MessageService() {

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
            log.info("connected to message queue");
        } catch (IOException | TimeoutException e) {
            log.error("failed to connect message queue", e);
        }
    }

    void onMessageReceived(Message message) {
        log.info("{}", message);
        try {
            messageProcessor.process(message);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }

    public void sendMessage(Message message) throws IOException {
        String msg = Utils.toJson(message);
        channel.basicPublish("", "message_outgoing_queue", null, msg.getBytes());
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
}
