package com.linker.connector;

import com.google.common.collect.ImmutableMap;
import com.linker.common.*;
import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Component
public class TestUtils {

    static MockKafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    public void setApplicationContext(MockKafkaExpressDelivery kafkaExpressDelivery) {
        this.kafkaExpressDelivery = kafkaExpressDelivery;
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

    public static TestUser connectClientUser(String userId) {
        TestUser testUser = new TestUser(userId);
        testUser.onConnected(e -> {
            testUser.send(new MessageContent(MessageType.AUTH_CLIENT,
                    ImmutableMap.of(
                            "appId", "app-id-343",
                            "userId", userId,
                            "token", "token-12345"
                    ), null, MessageFeature.RELIABLE, true));

        });

        testUser.connect();
        return testUser;
    }

    public static TestUser loginClientUser(String userId) throws TimeoutException {
        TestUser testUser = connectClientUser(userId);
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        String note = deliveredMessage.getMeta().getNote();
        String json = "{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"" + userId + "\",\"isAuthenticated\":true},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"" + note + "\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}";
        kafkaExpressDelivery.onMessageArrived(json.getBytes(StandardCharsets.UTF_8));
        kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_CONNECTED);
        testUser.getReceivedMessage(MessageType.AUTH_CLIENT_REPLY);
        return testUser;
    }

    public static void logout(TestUser testUser) throws TimeoutException {
        if (testUser != null) {
            testUser.close();
            kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED);
        }
    }
}
