package com.linker.connector.express;

import com.google.common.collect.ImmutableList;
import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.messageprocessors.MessageProcessorService;
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
    MessageProcessorService messageProcessorService;

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    ExpressDeliveryFactory expressDeliveryFactory;

    @Autowired
    Codec codec;

    Map<ExpressDeliveryType, ExpressDelivery> expressDeliveryMap;

    @PostConstruct
    public void setup() {
        expressDeliveryMap = ImmutableList.of(
                expressDeliveryFactory.createKafkaExpressDelivery(),
                expressDeliveryFactory.createNatsExpressDelivery()
        ).stream().peek(expressDelivery -> {
            expressDelivery.setListener(this);
            expressDelivery.start();
        }).collect(Collectors.toMap(ExpressDelivery::getType, r -> r));
    }

    public void deliveryMessage(Message message) throws IOException {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);
        expressDelivery.deliveryMessage(applicationConfig.getDeliveryTopics(), codec.serialize(message));
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, byte[] message) {
        try {
            Message msg = codec.deserialize(message, Message.class);
            log.info("message received from {}:{}", expressDelivery.getType(), msg);
            messageProcessorService.processOutgoingMessage(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }

    public void showdown() {
        log.info("showdown post office");
        expressDeliveryMap.values().forEach(ExpressDelivery::stop);
    }
}
