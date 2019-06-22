package com.linker.processor;


import com.google.common.collect.ImmutableList;
import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.common.messagedelivery.RabbitMQExpressDelivery;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostOffice implements ExpressDeliveryListener {

    @Autowired
    MessageProcessorService messageProcessor;

    @Autowired
    ApplicationConfig applicationConfig;

    Map<ExpressDeliveryType, ExpressDelivery> expressDeliveryMap;

    @PostConstruct
    public void setup() {
        String consumerTopics = applicationConfig.getConsumerTopics();
        expressDeliveryMap = ImmutableList.of(
                new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, "group-incoming"),
                new RabbitMQExpressDelivery(applicationConfig.getRabbitmqHosts(), consumerTopics),
                new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics)
        ).stream().peek(expressDelivery -> {
            expressDelivery.setListener(this);
            expressDelivery.start();
        }).collect(Collectors.toMap(ExpressDelivery::getType, r -> r));
    }

    public void deliveryMessage(Message message) throws IOException {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);
        String json = Utils.toJson(message);
        expressDelivery.deliveryMessage("connector-01", json);
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, String message) {
        try {
            Message msg = Utils.fromJson(message, Message.class);
            log.info("message arrived from {}:{}", expressDelivery.getType(), message);
            messageProcessor.process(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }
}
