package com.linker.processor.express;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.UserChannel;
import com.linker.processor.services.ClientAppService;
import com.linker.processor.services.DomainService;
import com.linker.processor.services.KafkaCacheService;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
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

    @Autowired
    KafkaCacheService kafkaCacheService;

    Map<ExpressDeliveryType, ExpressDelivery> expressDeliveryMap;

    @PostConstruct
    public void setup() {
        expressDeliveryMap = ImmutableList.of(
                expressDeliveryFactory.createKafkaExpressDelivery(kafkaCacheService),
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

        if (message.getContent().getType() == MessageType.INTERNAL_MESSAGE) {
            throw new IllegalStateException("message should not be INTERNAL_MESSAGE - " + message.toString());
        }

        sendToConnector(message);
    }

    void deliverMessageOutsideDomain(Message message) {
        log.info("cross domain message");
        processorUtils.assertInternalMessage(message);
        final String targetDomainName = message.getMeta().getTargetAddress().getDomainName();
        String nextDomainName = domainService.getNextDomainName(targetDomainName);
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
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);
        try {
            for (Address address : addressList) {
                message.getMeta().setTargetAddress(address);
                expressDelivery.deliverMessage(address.getConnectorName(), codec.serialize(message));
            }
        } catch (IOException e) {
            log.error("failed to deliver message {}, persist", message, e);
            messageProcessor.persistMessage(message, MessageState.NETWORK_ERROR);
        }
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, byte[] message) {
        Message msg = null;
        try {
            msg = codec.deserialize(message, Message.class);
            log.info("message arrived from {}:{}", expressDelivery.getType(), msg);
            messageProcessor.process(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing, persist", e);
            if (msg != null) {
                messageProcessor.persistMessage(msg, MessageState.ERROR);
            }
        }
    }


    Set<Address> getRouteTargetAddresses(Message message) {
        String to = message.getTo();
        if (StringUtils.isNotBlank(to)) {
            if (processorUtils.isConnectorMessage(message)) {
                return ImmutableSet.of(new Address(applicationConfig.getDomainName(), to));
            }

            UserChannel userChannel = userChannelService.getById(message.getTo());
            if (userChannel != null) {
                if (message.getMeta().getDeliveryType() == DeliveryType.ALL) {
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

    public void shutdown() {
        log.info("showdown post office");
        expressDeliveryMap.values().forEach(ExpressDelivery::stop);
    }
}
