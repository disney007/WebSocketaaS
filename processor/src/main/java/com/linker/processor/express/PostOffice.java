package com.linker.processor.express;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.codec.Codec;
import com.linker.common.exceptions.ProcessMessageException;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.UserChannel;
import com.linker.processor.services.ClientAppService;
import com.linker.processor.services.DomainService;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostOffice implements ExpressDeliveryListener {

    @Autowired
    MessageProcessorService messageProcessor;

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    UserChannelService userChannelService;

    @Autowired
    ExpressDeliveryFactory expressDeliveryFactory;

    @Autowired
    ClientAppService clientAppService;

    @Autowired
    ProcessorUtils processorUtils;

    @Autowired
    DomainService domainService;

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

    void adjustDeliveryType(Message message) {
        if (message.getMeta().getDeliveryType() == DeliveryType.ALL) {
            String to = message.getTo();
            if (message.getContent().getType() == MessageType.INTERNAL_MESSAGE
                    || clientAppService.isMasterUserId(to)
                    || StringUtils.startsWithIgnoreCase(to, Keywords.PROCESSOR)) {
                message.getMeta().setDeliveryType(DeliveryType.ANY);
            }
        }
    }

    void populateTargetAddress(Message message) {
        MessageMeta meta = message.getMeta();
        if (meta.getTargetAddress() == null) {
            String domainName = clientAppService.resolveDomain(message.getTo());
            meta.setTargetAddress(new Address(domainName, null, -1L));
        }
    }

    public void deliverMessage(Message message) {
        populateTargetAddress(message);

        if (processorUtils.isCurrentDomainMessage(message)) {
            deliverMessageInsideDomain(message);
        } else {
            deliverMessageOutsideDomain(message);
        }
    }

    void deliverMessageInsideDomain(Message message) {
        log.info("same domain message");
        sendToConnector(message);
    }

    void deliverMessageOutsideDomain(Message message) {
        log.info("cross domain message");
        processorUtils.assertInternalMessage(message);
        final String targetDomainName = message.getMeta().getTargetAddress().getDomainName();
        String nextDomainName = domainService.getNextDomainName(targetDomainName);
        if (StringUtils.isBlank(nextDomainName)) {
            throw new ProcessMessageException(String.format("can not resolve next domain name from domain [%s] to domain [%s]", applicationConfig.getDomainName(), targetDomainName));
        }
        log.info("send message from domain [{}] to next domain [{}] for target domain name [{}]", applicationConfig.getDomainName(),
                nextDomainName, targetDomainName);

        message.setTo(processorUtils.resolveDomainUserId(nextDomainName));
        sendToConnector(message);

    }

    // processor -> connector
    void sendToConnector(Message message) {

        adjustDeliveryType(message);
        Set<Address> targetAddresses = getRouteTargetAddresses(message);
        if (targetAddresses.isEmpty()) {
            log.info("address not found for {}, persist", message);
            messageProcessor.persistMessage(message, MessageState.ADDRESS_NOT_FOUND);
            return;
        }

        sendToAddresses(targetAddresses, message);
    }

    void sendToAddresses(Set<Address> addressList, Message message) {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("deliver message with {}:{}", expressDelivery.getType(), message);
        for (Address address : addressList) {
            message.getMeta().setTargetAddress(address);
            expressDelivery.deliverMessage(address.getConnectorName(), codec.serialize(message));
        }
    }

    @Override
    public void onMessageDeliveryFailed(ExpressDelivery expressDelivery, byte[] message) {
        Message msg = codec.deserialize(message, Message.class);
        log.error("failed to deliver message {}, persist", message);
        messageProcessor.persistMessage(msg, MessageState.NETWORK_ERROR);
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, byte[] message) {
        ;
        try {
            Message msg = codec.deserialize(message, Message.class);
            onMessageArrived(msg, expressDelivery.getType().name());
        } catch (Exception e) {
            log.error("can not deserialize message", e);
        }
    }

    public void onMessageArrived(Message message, String from) {
        try {
            log.info("message arrived from {}:{}", from, message);
            messageProcessor.process(message);
        } catch (Exception e) {
            log.error("error occurred during message processing, persist", e);
            messageProcessor.persistMessage(message, MessageState.PROCESSOR_ERROR);
        }
    }

    public Set<Address> getRouteTargetAddresses(Message message) {
        return getRouteTargetAddresses(message.getTo(), message.getMeta().getDeliveryType());
    }

    public Set<Address> getRouteTargetAddresses(String to, DeliveryType deliveryType) {
        if (StringUtils.isNotBlank(to)) {
            if (processorUtils.isConnectorUser(to)) {
                return ImmutableSet.of(new Address(applicationConfig.getDomainName(), to));
            }

            UserChannel userChannel = userChannelService.getById(to);
            if (userChannel != null) {
                if (deliveryType == DeliveryType.ALL) {
                    return userChannel.getAddresses();
                } else {
                    Address address = Utils.getRandomItemInCollection(userChannel.getAddresses());
                    if (address != null) {
                        return ImmutableSet.of(address);
                    }
                }
            }
        }

        return ImmutableSet.of();
    }

    public void stopIncoming() {
        log.info("post office stop incoming");
        expressDeliveryMap.values().forEach(ExpressDelivery::stopConsumer);
    }

    public void stopOutging() {
        log.info("post office stop outgoing");
        expressDeliveryMap.values().forEach(ExpressDelivery::stopProducer);
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }
}
