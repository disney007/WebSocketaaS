package com.linker.connector;

import com.linker.common.Message;
import com.linker.connector.messagedelivery.ExpressDelivery;
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

    public void deliveryMessage(Message message) throws IOException {
        getExpressDelivery().deliveryMessage(message);
    }

    ExpressDelivery getExpressDelivery() {
        return rabbitMQExpressDelivery;
    }

    public void onMessageArrived(Message message) {
        try {
            messageProcessorService.processOutgoingMessage(message);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }
}
