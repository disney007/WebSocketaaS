package com.linker.processor.express;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.codec.Codec;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.common.exceptions.RouterNotConnectedException;
import com.linker.common.messagedelivery.ExpressDelivery;
import com.linker.common.messagedelivery.ExpressDeliveryListener;
import com.linker.common.messagedelivery.ExpressDeliveryType;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.UserChannel;
import com.linker.processor.services.ClientAppService;
import com.linker.processor.services.RouterService;
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
    RouterService routerService;

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
            if (clientAppService.isMasterUserId(to) || StringUtils.startsWithIgnoreCase(to, "processor")) {
                message.getMeta().setDeliveryType(DeliveryType.ANY);
            }
        }
    }

    String populateTargetAddress(Message message) {
        MessageMeta meta = message.getMeta();
        if (meta.getTargetAddress() == null) {
            String domainName = clientAppService.resolveDomain(message.getTo());
            meta.setTargetAddress(new Address(domainName, null, -1L));
        }

        return meta.getTargetAddress().getDomainName();
    }

    public void deliveryMessage(Message message) throws IOException {
        String targetDomainName = populateTargetAddress(message);

        if (StringUtils.equalsIgnoreCase(targetDomainName, applicationConfig.getDomainName())) {
            deliverSameDomainMessage(message);
        } else {
            deliverCrossDomainMessage(message);
        }
    }

    void deliverSameDomainMessage(Message message) throws IOException {
        log.info("same domain message");
        sendToConnector(message);
    }

    void deliverCrossDomainMessage(Message message) throws IOException {
        log.info("cross domain message");
        String targetDomainName = populateTargetAddress(message);
        if (processorUtils.isDomainRouter()) {
            if (routerService.isDomainLinkedToCurrentRouter(targetDomainName)) {
                log.info("target domain name {} is linked to current router {}", targetDomainName, routerService.getRouterName());
                sendToDomain(message);
            } else {
                log.info("target domain name {} is not linked to current router {}", targetDomainName, routerService.getRouterName());
                sendToNextRouter(message);
            }
        } else {
            // if message is in current domain, send to connector
            if (StringUtils.equalsIgnoreCase(applicationConfig.getDomainName(), targetDomainName)) {
                log.info("message [{}] is in current domain", message);
                sendToConnector(message);
            } else {
                log.info("message [{}] is in another domain [{}]", message, targetDomainName);
                sendToRouter(message);
            }
        }
    }

    // domain -> router
    public void sendToRouter(Message message) {
        try {
            routerService.sendMessage(message);
        } catch (RouterNotConnectedException e) {
            log.warn("router not connected exception occurred", e);
            throw new AddressNotFoundException(message, e);
        }
    }

    // router -> router
    void sendToNextRouter(Message message) throws IOException {
        processorUtils.assertInternalMessage(message);

        final String targetDomainName = message.getMeta().getTargetAddress().getDomainName();
        String nextRouterName = routerService.getNextRouterName(targetDomainName);
        log.info("send message from router [{}] to next router [{}] for target domain name [{}]", applicationConfig.getDomainName(),
                nextRouterName, targetDomainName);

        message.setTo(processorUtils.resolveRouterUserId(nextRouterName));
        sendToConnector(message);
    }

    // router -> domain
    void sendToDomain(Message message) throws IOException {
        processorUtils.assertInternalMessage(message);

        String targetDomainName = message.getMeta().getTargetAddress().getDomainName();
        log.info("send message from router [{}] to domain [{}] - {}", applicationConfig.getDomainName(),
                targetDomainName, message);
        message.setTo(processorUtils.resolveRouterUserId(targetDomainName));
        sendToConnector(message);
    }

    // domain -> connector
    void sendToConnector(Message message) throws IOException {
        ExpressDelivery expressDelivery = getExpressDelivery(message);
        log.info("delivery message with {}:{}", expressDelivery.getType(), message);

        adjustDeliveryType(message);
        Set<Address> targetAddresses = getRouteTargetAddresses(message);
        if (targetAddresses.isEmpty()) {
            throw new AddressNotFoundException(message);
        }
        for (Address address : targetAddresses) {
            message.getMeta().setTargetAddress(address);
            expressDelivery.deliveryMessage(address.getConnectorName(), codec.serialize(message));
        }
    }

    ExpressDelivery getExpressDelivery(Message message) {
        ExpressDeliveryType type = Utils.calcExpressDelivery(message.getContent().getFeature());
        return this.expressDeliveryMap.get(type);
    }

    @Override
    public void onMessageArrived(ExpressDelivery expressDelivery, byte[] message) {
        try {
            Message msg = codec.deserialize(message, Message.class);
            log.info("message arrived from {}:{}", expressDelivery.getType(), msg);
            messageProcessor.process(msg);
        } catch (Exception e) {
            log.error("error occurred during message processing", e);
        }
    }

    boolean isConnectorMessage(Message message) {
        String to = message.getTo();
        return StringUtils.isNotBlank(to) && StringUtils.startsWithIgnoreCase(to, Keywords.CONNECTOR);
    }

    Set<Address> getRouteTargetAddresses(Message message) {
        String to = message.getTo();
        if (StringUtils.isNotBlank(to)) {
            if (isConnectorMessage(message)) {
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
