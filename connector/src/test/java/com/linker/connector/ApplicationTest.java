package com.linker.connector;

import com.google.common.collect.ImmutableSet;
import com.linker.common.MessageType;
import com.linker.common.messages.UserDisconnected;
import com.linker.connector.express.MockKafkaExpressDelivery;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ApplicationTest extends IntegrationTest {

    @Autowired
    Application application;
    @Autowired
    MockKafkaExpressDelivery kafkaExpressDelivery;

    @Test
    @Ignore // this test will destroy the whole environment, which should be executed at last
    public void test_shutdown() throws TimeoutException {
        Set<String> userIdList = ImmutableSet.of("user1", "user2", "user3");
        for (String userId : userIdList) {
            TestUtils.loginClientUser(userId);
        }

        application.shutdown();
        Set<String> disconnectedUserIdList = ImmutableSet.of(
                kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED),
                kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED),
                kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED))
                .stream()
                .map(msg -> msg.getContent().getData(UserDisconnected.class))
                .map(UserDisconnected::getUserId)
                .collect(Collectors.toSet());
        assertEquals(userIdList, disconnectedUserIdList);
    }
}
