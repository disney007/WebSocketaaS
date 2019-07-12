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

import static org.junit.Assert.assertEquals;

@Component
public class TestUtils {
    static KafkaExpressDelivery kafkaExpressDelivery;

    public TestUtils(KafkaExpressDelivery kafkaExpressDelivery) {
        TestUtils.kafkaExpressDelivery = kafkaExpressDelivery;
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
        return new TestUser(userId, address);
    }

    public static TestUser loginUser(String userId) throws JsonProcessingException {
        return loginUser(userId, new Address("domain-01", "connector-01", 10L));
    }
}
