package com.linker.processor.messagedelivery;


import com.linker.common.Message;
import com.linker.processor.messageprocessors.MessageProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class PostOffice {

    @Autowired
    MessageProcessorService messageProcessor;

    @Autowired
    RabbitMQExpressDelivery rabbitMQExpressDelivery;

    public PostOffice() {

    }

    void onMessageArrived(Message message) {
        log.info("{}", message);
        try {
            messageProcessor.process(message);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }

    public void deliveryMessage(Message message) throws IOException {
        getExpressDelivery().deliveryMessage(message);
    }

    ExpressDelivery getExpressDelivery() {
        return rabbitMQExpressDelivery;
    }

}
