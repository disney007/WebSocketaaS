package com.linker.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.Utils;
import com.linker.common.messagedelivery.KafkaExpressDelivery;
import com.linker.common.messages.UserConnected;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@Component
public class TestUtils {
    static KafkaExpressDelivery kafkaExpressDelivery;

    public TestUtils(KafkaExpressDelivery kafkaExpressDelivery) {
        TestUtils.kafkaExpressDelivery = kafkaExpressDelivery;
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

    public static TestUser loginUser(String userId, Address address) throws JsonProcessingException {
        Message message = Message.builder()
                .from(Keywords.SYSTEM)
                .content(
                        MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnected(userId),
                                MessageFeature.RELIABLE)
                )
                .meta(new MessageMeta(address))
                .build();
        kafkaExpressDelivery.onMessageArrived(Utils.toJson(message));
        return new TestUser(userId, address, message.getId());
    }

    public static TestUser loginUser(String userId) throws JsonProcessingException {
        return loginUser(userId, new Address("domain-01", "connector-01", 10L));
    }

    public static TestUser loginUser(String userId, Long socketId) throws JsonProcessingException {
        return loginUser(userId, new Address("domain-01", "connector-01", socketId));
    }
}
