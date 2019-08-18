package com.linker.processor;

import com.linker.common.*;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messages.UserConnected;
import com.linker.common.messages.UserDisconnected;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@Component
public class TestUtils {
    static KafkaExpressDelivery kafkaExpressDelivery;
    static Codec codec;
    static ProcessorUtils processorUtils;

    public TestUtils(KafkaExpressDelivery kafkaExpressDelivery, Codec codec, ProcessorUtils processorUtils) {
        TestUtils.kafkaExpressDelivery = kafkaExpressDelivery;
        TestUtils.codec = codec;
        TestUtils.processorUtils = processorUtils;
    }

    public static List<Message> sortMessages(List<Message> messages) {
        messages.sort(Comparator.comparing(Message::getTo).thenComparing(Message::getFrom));
        return messages;
    }

    public static void messagesEqual(List<Message> expectedMsgs, List<Message> actualMsgs) {
        sortMessages(expectedMsgs);
        sortMessages(actualMsgs);
        assertEquals(expectedMsgs.size(), actualMsgs.size());
        IntStream.range(0, expectedMsgs.size())
                .forEach(index -> TestUtils.messageEquals(expectedMsgs.get(index), actualMsgs.get(index)));
    }

    public static void messageEquals(Message expectedMsg, Message actualMsg) {
        Object actualData = Utils.convert(actualMsg.getContent().getData(), expectedMsg.getContent().getData().getClass());
        actualMsg.getContent().setData(actualData);

        assertEquals(expectedMsg.getVersion(), actualMsg.getVersion());
        assertEquals(expectedMsg.getContent(), actualMsg.getContent());
        assertEquals(expectedMsg.getFrom(), actualMsg.getFrom());
        assertEquals(expectedMsg.getTo(), actualMsg.getTo());
        assertEquals(expectedMsg.getMeta(), actualMsg.getMeta());
        assertEquals(expectedMsg.getState(), actualMsg.getState());
    }

    public static void internalMessageEquals(Message expectedMsg, Message actualMsg) {
        messageEquals(
                Utils.convert(expectedMsg.getContent().getData(), Message.class),
                Utils.convert(actualMsg.getContent().getData(), Message.class)
        );

        Message clonedExpectedMsg = expectedMsg.clone();
        clonedExpectedMsg.getContent().setData("");
        Message clonedActualMsg = actualMsg.clone();
        clonedActualMsg.getContent().setData("");
        messageEquals(clonedExpectedMsg, clonedActualMsg);
    }

    public static TestUser loginUser(String userId, Address address) {
        Message message = Message.builder()
                .from(Keywords.SYSTEM)
                .to(Keywords.PROCESSOR)
                .content(
                        MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnected(userId),
                                MessageFeature.RELIABLE)
                )
                .meta(new MessageMeta(address))
                .build();
        kafkaExpressDelivery.onMessageArrived(codec.serialize(message));
        return new TestUser(userId, address, message.getId(), null);
    }

    public static TestUser loginUser(String userId) {
        return loginUser(userId, new Address("domain-01", "connector-01", 10L));
    }

    public static TestUser loginUser(String userId, Long socketId) {
        return loginUser(userId, new Address("domain-01", "connector-01", socketId));
    }

    public static TestUser loginDomainUser(String userId, Long socketId) {
        return loginUser(processorUtils.resolveDomainUserId(userId), socketId);
    }

    public static void logoutUser(TestUser user) {
        Message message = Message.builder()
                .from(Keywords.SYSTEM)
                .to(Keywords.PROCESSOR)
                .content(
                        MessageUtils.createMessageContent(MessageType.USER_DISCONNECTED, new UserDisconnected(user.getUserId()),
                                MessageFeature.RELIABLE)
                )
                .meta(new MessageMeta(user.getAddress()))
                .build();
        user.setDisconnectedMessageId(message.getId());
        kafkaExpressDelivery.onMessageArrived(codec.serialize(message));
    }
}
