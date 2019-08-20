package com.linker.connector.express;

import com.google.common.collect.ImmutableList;
import com.linker.common.*;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.common.messages.MessageStateChanged;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.messageprocessors.MessageProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    public void deliverStateChangedMessage(Message message, MessageState newState) {
        Message newMsg = Message.builder()
                .from(Keywords.SYSTEM)
                .to(Keywords.PROCESSOR)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName())))
                .content(
                        MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(message, newState), MessageFeature.RELIABLE)
                )
                .build();
        deliverMessage(newMsg);
    }

    public void deliverMessage(Message message) {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("deliver message with {}:{}", expressDelivery.getType(), message);
        expressDelivery.deliverMessage(applicationConfig.getDeliveryTopics(), codec.serialize(message));
    }

    @Override
    public void onMessageDeliveryFailed(ExpressDelivery expressDelivery, byte[] message) {
        // TODO: handle message delivery failed
        log.error("TODO: handle message delivery failed");
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, byte[] message) {
        Message msg = null;
        try {
            msg = codec.deserialize(message, Message.class);
            log.info("message received from {}:{}", expressDelivery.getType(), msg);
            messageProcessorService.processOutgoingMessage(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
            if (msg != null && msg.getContent().getFeature() == MessageFeature.RELIABLE) {
                deliverStateChangedMessage(msg, MessageState.CONNECTOR_ERROR);
            }
        }
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }

    public void stopIncoming(){
        log.info("post office stop incoming");
        expressDeliveryMap.values().forEach(ExpressDelivery::stopConsumer);
    }

    public void stopOutging() {
        log.info("post office stop outgoing");
        expressDeliveryMap.values().forEach(ExpressDelivery::stopProducer);
    }


}
