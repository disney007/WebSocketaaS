package com.linker.processor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.Utils;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messagedelivery.NatsExpressDelivery;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.UserChannel;
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

    Map<ExpressDeliveryType, ExpressDelivery> expressDeliveryMap;

    @PostConstruct
    public void setup() {
        String consumerTopics = applicationConfig.getConsumerTopics();
        expressDeliveryMap = ImmutableList.of(
                new KafkaExpressDelivery(applicationConfig.getKafkaHosts(), consumerTopics, "group-incoming"),
                new NatsExpressDelivery(applicationConfig.getNatsHosts(), consumerTopics)
        ).stream().peek(expressDelivery -> {
            expressDelivery.setListener(this);
            expressDelivery.start();
        }).collect(Collectors.toMap(ExpressDelivery::getType, r -> r));
    }

    public void deliveryMessage(Message message) throws IOException {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);


        Set<Address> targetAddresses = getRouteTargetAddresses(message);
        if (targetAddresses.isEmpty()) {
            throw new AddressNotFoundException(message);
        }
        for (Address address : targetAddresses) {
            message.getMeta().setTargetAddress(address);
            String json = Utils.toJson(message);
            expressDelivery.deliveryMessage(address.getConnectorName(), json);
        }
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

    Set<Address> getRouteTargetAddresses(Message message) {
        String to = message.getTo();
        if (StringUtils.isNotBlank(to)) {
            if (StringUtils.startsWithIgnoreCase(to, "connector")) {
                return ImmutableSet.of(new Address(applicationConfig.getDomainName(), to));
            }

            UserChannel userChannel = userChannelService.getById(message.getTo());
            if (userChannel != null) {
                return userChannel.getAddresses();
            }
        }

        return ImmutableSet.of();
    }

    public void shutdown() {
        log.info("showdown post office");
        expressDeliveryMap.values().forEach(ExpressDelivery::stop);
    }
}
