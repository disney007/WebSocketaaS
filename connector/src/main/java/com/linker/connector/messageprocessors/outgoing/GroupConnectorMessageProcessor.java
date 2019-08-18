package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.*;
import com.linker.common.messages.GroupConnectorMessage;
import com.linker.common.models.ConnectorUserId;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.messageprocessors.MessageProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupConnectorMessageProcessor extends OutgoingMessageProcessor<GroupConnectorMessage> {
    final MessageProcessorService messageProcessorService;
    final ApplicationConfig applicationConfig;

    @Override
    public MessageType getMessageType() {
        return MessageType.GROUP_CONNECTOR_MESSAGE;
    }

    @Override
    public void doProcess(Message message, GroupConnectorMessage data, MessageContext context) {
        data.getConnectorUserIds().forEach(userId -> processUser(userId, message, data));
    }

    void processUser(ConnectorUserId connectorUserId, Message message, GroupConnectorMessage data) {
        MessageContent messageContent = MessageUtils.createMessageContent(MessageType.MESSAGE, data.getContent(), message.getContent().getFeature());
        messageContent.setReference(message.getContent().getReference());
        Message msg = Message.builder()
                .from(message.getFrom())
                .to(connectorUserId.getUserId())
                .content(messageContent)
                .meta(new MessageMeta(message.getMeta().getOriginalAddress(),
                        new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), connectorUserId.getSocketId())))
                .build();
        messageProcessorService.processOutgoingMessage(msg);
    }
}
