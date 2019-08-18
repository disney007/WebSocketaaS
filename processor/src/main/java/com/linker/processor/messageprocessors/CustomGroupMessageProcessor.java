package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.GroupConnectorMessage;
import com.linker.common.messages.GroupMessage;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.common.models.ConnectorUserId;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.express.PostOffice;
import com.linker.processor.services.ClientAppService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomGroupMessageProcessor extends MessageProcessor<GroupMessage> {

    final MessageProcessorService messageProcessorService;
    final ClientAppService clientAppService;
    final PostOffice postOffice;
    final ApplicationConfig applicationConfig;

    @Override
    public MessageType getMessageType() {
        return MessageType.GROUP_MESSAGE;
    }

    @Override
    public void doProcess(Message message, GroupMessage data, MessageContext context) {

        Map<String, Set<String>> domainGroupedUsers = new HashMap<>();

        data.getTo().forEach(toUser -> {
            String domain = clientAppService.resolveDomain(toUser);
            if (!domainGroupedUsers.containsKey(domain)) {
                domainGroupedUsers.put(domain, new HashSet<>());
            }
            domainGroupedUsers.get(domain).add(toUser);
        });

        domainGroupedUsers.forEach((domain, userIds) -> {
            if (StringUtils.equalsAnyIgnoreCase(applicationConfig.getDomainName(), domain)) {
                processCurrentDomain(userIds, message, data);
            } else {
                processOtherDomain(domain, userIds, message, data);
            }
        });
    }

    void processCurrentDomain(Set<String> userIds, Message message, GroupMessage data) {
        log.info("process group message in current domain [{}]", applicationConfig.getDomainName());
        Map<String, Set<ConnectorUserId>> connectorGroupedUsers = new HashMap<>();

        userIds.forEach(toUser -> {
            DeliveryType type = clientAppService.isMasterUserId(toUser) ? DeliveryType.ANY : DeliveryType.ALL;
            Set<Address> addresses = postOffice.getRouteTargetAddresses(toUser, type);
            if (addresses.size() == 0) {
                processUserAddressNotFound(toUser, message, data);
            }

            addresses.forEach(address -> {
                if (!connectorGroupedUsers.containsKey(address.getConnectorName())) {
                    connectorGroupedUsers.put(address.getConnectorName(), new HashSet<>());
                }
                connectorGroupedUsers.get(address.getConnectorName()).add(new ConnectorUserId(toUser, address.getSocketId()));
            });
        });

        connectorGroupedUsers.forEach((connectorName, connectorUserIds) -> {
            processConnectorUserMessages(connectorName, connectorUserIds, message, data);
        });
    }

    void processConnectorUserMessages(String connectorName, Set<ConnectorUserId> connectorUserIds, Message message, GroupMessage data) {

        MessageContent messageContent = MessageUtils.createMessageContent(MessageType.GROUP_CONNECTOR_MESSAGE,
                new GroupConnectorMessage(connectorUserIds, new MessageForward(message.getFrom(), data.getContent())), message.getContent().getFeature());
        messageContent.setConfirmationEnabled(false);
        messageContent.setReference(message.getContent().getReference());

        Message connectorMessage = Message.builder()
                .from(message.getFrom())
                .to(connectorName)
                .meta(new MessageMeta(message.getMeta().getOriginalAddress()))
                .content(messageContent)
                .build();
        postOffice.deliverMessage(connectorMessage);
    }

    void processUserAddressNotFound(String toUser, Message message, GroupMessage data) {
        MessageContent content = MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageRequest(toUser, data.getContent()), message.getContent().getFeature());
        content.setConfirmationEnabled(false);
        content.setReference(message.getContent().getReference());

        Message msg = Message.builder()
                .from(message.getFrom())
                .to(toUser)
                .meta(new MessageMeta(message.getMeta().getOriginalAddress()))
                .content(content)
                .build();
        messageProcessorService.process(msg);
    }

    void processOtherDomain(String domainName, Set<String> userIds, Message message, GroupMessage data) {
        log.info("send group message to domain [{}]", domainName);
        Message subMessage = Message.builder()
                .from(message.getFrom())
                .to(Keywords.PROCESSOR)
                .meta(new MessageMeta(message.getMeta().getOriginalAddress(), new Address(domainName, null, -1L)))
                .createdAt(message.getCreatedAt())
                .content(MessageUtils.createMessageContent(MessageType.GROUP_MESSAGE, new GroupMessage(userIds, data.getContent()), message.getContent().getFeature()))
                .build();
        messageProcessorService.process(subMessage);
    }
}
