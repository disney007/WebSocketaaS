package com.linker.processor.messageprocessors;

import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.messages.GroupConnectorMessage;
import com.linker.common.messages.GroupMessage;
import com.linker.common.messages.MessageForward;
import com.linker.common.models.ConnectorUserId;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CustomGroupMessageProcessorTest extends IntegrationTest {

    @Before
    public void setup() {
        TestUtils.loginDomainUser("domain-04", 111L);
        TestUtils.loginDomainUser("domain-02", 110L);
    }

    Message createExpectedInternalMessage(String to, Set<String> userIds, String targetDomainName, Long socketId, Message originalMessage) {
        return MessageUtils.touchMessage(Message.builder()
                .from("ANZ-123223")
                .to(to)
                .content(MessageUtils.createMessageContent(MessageType.INTERNAL_MESSAGE,
                        MessageUtils.touchMessage(
                                Message.builder()
                                        .from("ANZ-123223")
                                        .to(Keywords.PROCESSOR)
                                        .content(MessageUtils.createMessageContent(MessageType.GROUP_MESSAGE,
                                                new GroupMessage(userIds, "some thing here to send"),
                                                originalMessage.getContent().getFeature()))
                                        .meta(new MessageMeta(originalMessage.getMeta().getOriginalAddress(), new Address(targetDomainName, null, -1L)))
                                        .build())
                        , originalMessage.getContent().getFeature()))
                .meta(new MessageMeta(originalMessage.getMeta().getOriginalAddress(), new Address("domain-01", "connector-01", socketId)))
                .build());
    }

    Message createExpectedGroupConnectorMessage(String to, Set<ConnectorUserId> userIds, Message originalMessage) {
        MessageContent content = MessageUtils.createMessageContent(MessageType.GROUP_CONNECTOR_MESSAGE,
                new GroupConnectorMessage(userIds, new MessageForward("ANZ-123223", "some thing here to send"))
                , originalMessage.getContent().getFeature());
        content.setConfirmationEnabled(false);
        return Message.builder()
                .from("ANZ-123223")
                .to(to)
                .content(content)
                .meta(new MessageMeta(originalMessage.getMeta().getOriginalAddress(), new Address("domain-01", to, -1L)))
                .build();
    }

    @Test
    public void test_reliable_processing() throws TimeoutException {
        TestUtils.loginUser("ANZ-1232122", new Address("domain-01", "connector-01", 10L));
        TestUtils.loginUser("ANZ-1232123", new Address("domain-01", "connector-01", 11L));
        TestUtils.loginUser("ANZ-1232124", new Address("domain-01", "connector-02", 12L));

        Message incomingMessage = createMessage(MessageFeature.RELIABLE);
        givenMessage(incomingMessage);

        Message msgDomain04 = kafkaExpressDelivery.getDeliveredMessage(MessageType.INTERNAL_MESSAGE);
        Message msgDomain02 = kafkaExpressDelivery.getDeliveredMessage(MessageType.INTERNAL_MESSAGE);
        Message msgDomain01Connector01 = kafkaExpressDelivery.getDeliveredMessage(MessageType.GROUP_CONNECTOR_MESSAGE);
        Message msgDomain01Connector02 = kafkaExpressDelivery.getDeliveredMessage(MessageType.GROUP_CONNECTOR_MESSAGE);


        TestUtils.internalMessageEquals(
                createExpectedInternalMessage("domain2-1", ImmutableSet.of("ANZ-2232122", "ANZ-2232123"), "domain-02", 110L, incomingMessage),
                msgDomain02);

        TestUtils.internalMessageEquals(
                createExpectedInternalMessage("domain4-1", ImmutableSet.of("ANZ-6232122", "ANZ-6232123"), "domain-04", 111L, incomingMessage),
                msgDomain04);

        TestUtils.messageEquals(
                createExpectedGroupConnectorMessage("connector-01", ImmutableSet.of(
                        new ConnectorUserId("ANZ-1232122", 10L),
                        new ConnectorUserId("ANZ-1232123", 11L)
                ), incomingMessage),
                msgDomain01Connector01
        );

        TestUtils.messageEquals(
                createExpectedGroupConnectorMessage("connector-02", ImmutableSet.of(
                        new ConnectorUserId("ANZ-1232124", 12L)
                ), incomingMessage),
                msgDomain01Connector02
        );

        // check saved messages
        Message targetNotFoundMessage = Message.builder()
                .from(incomingMessage.getFrom())
                .to("ANZ-1232125")
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE,
                        new MessageForward(incomingMessage.getFrom(), "some thing here to send"), MessageFeature.RELIABLE))
                .meta(new MessageMeta(incomingMessage.getMeta().getOriginalAddress(), new Address("domain-01", null, -1L)))
                .state(MessageState.ADDRESS_NOT_FOUND)
                .build();
        targetNotFoundMessage.getContent().setConfirmationEnabled(false);
        MessageUtils.touchMessage(targetNotFoundMessage);

        List<Message> savedMessages = messageRepository.findMessagesByTypes(ImmutableSet.of(MessageType.MESSAGE), 100)
                .get().collect(Collectors.toList());
        assertEquals(1, savedMessages.size());
        TestUtils.messageEquals(targetNotFoundMessage, savedMessages.get(0));
    }

    @Test
    public void test_fast_processing() throws TimeoutException {
        TestUtils.loginUser("ANZ-1232122", new Address("domain-01", "connector-01", 10L));
        TestUtils.loginUser("ANZ-1232123", new Address("domain-01", "connector-01", 11L));
        TestUtils.loginUser("ANZ-1232124", new Address("domain-01", "connector-02", 12L));

        Message incomingMessage = createMessage(MessageFeature.FAST);
        givenMessage(incomingMessage);

        Message msgDomain04 = natsExpressDelivery.getDeliveredMessage(MessageType.INTERNAL_MESSAGE);
        Message msgDomain02 = natsExpressDelivery.getDeliveredMessage(MessageType.INTERNAL_MESSAGE);
        Message msgDomain01Connector01 = natsExpressDelivery.getDeliveredMessage(MessageType.GROUP_CONNECTOR_MESSAGE);
        Message msgDomain01Connector02 = natsExpressDelivery.getDeliveredMessage(MessageType.GROUP_CONNECTOR_MESSAGE);


        TestUtils.internalMessageEquals(
                createExpectedInternalMessage("domain2-1", ImmutableSet.of("ANZ-2232122", "ANZ-2232123"), "domain-02", 110L, incomingMessage),
                msgDomain02);

        TestUtils.internalMessageEquals(
                createExpectedInternalMessage("domain4-1", ImmutableSet.of("ANZ-6232122", "ANZ-6232123"), "domain-04", 111L, incomingMessage),
                msgDomain04);

        TestUtils.messageEquals(
                createExpectedGroupConnectorMessage("connector-01", ImmutableSet.of(
                        new ConnectorUserId("ANZ-1232122", 10L),
                        new ConnectorUserId("ANZ-1232123", 11L)
                ), incomingMessage),
                msgDomain01Connector01
        );

        TestUtils.messageEquals(
                createExpectedGroupConnectorMessage("connector-02", ImmutableSet.of(
                        new ConnectorUserId("ANZ-1232124", 12L)
                ), incomingMessage),
                msgDomain01Connector02
        );

        List<Message> savedMessages = messageRepository.findMessagesByTypes(ImmutableSet.of(MessageType.MESSAGE), 100)
                .get().collect(Collectors.toList());
        assertEquals(0, savedMessages.size());

    }

    Message createMessage(MessageFeature feature) {
        Set<String> users = ImmutableSet.of("ANZ-1232122", "ANZ-1232123", "ANZ-1232124", "ANZ-1232125", // domain-01
                "ANZ-2232122", "ANZ-2232123", // domain-02
                "ANZ-6232122", "ANZ-6232123" // domain-04
        );

        return Message.builder()
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                .content(MessageUtils.createMessageContent(MessageType.GROUP_MESSAGE,
                        new GroupMessage(users, "some thing here to send"), feature))
                .build();
    }
}
