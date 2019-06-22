package com.linker.connector;

import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.connector.messagedelivery.ExpressDelivery;
import com.linker.connector.messagedelivery.KafkaExpressDelivery;
import com.linker.connector.messagedelivery.RabbitMQExpressDelivery;
import com.linker.connector.messageprocessors.MessageProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class PostOffice {

    @Autowired
    MessageProcessorService messageProcessorService;

    @Autowired
    RabbitMQExpressDelivery rabbitMQExpressDelivery;

    @Autowired
    KafkaExpressDelivery kafkaExpressDelivery;

    public void deliveryMessage(Message message) throws IOException {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);
        String json = Utils.toJson(message);
        expressDelivery.deliveryMessage(json);
    }

    public void onMessageArrived(String message, ExpressDelivery expressDelivery) {
        try {
            Message msg = Utils.fromJson(message, Message.class);
            log.info("message received from {}:{}", expressDelivery.getType(), message);
            messageProcessorService.processOutgoingMessage(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }

    ExpressDelivery getExpressDelivery(Message message) {
        switch (message.getContent().getFeature()) {
            case FAST:
                return null;
            case STABLE:
                return rabbitMQExpressDelivery;
            default:
                return kafkaExpressDelivery;
        }
    }
}
