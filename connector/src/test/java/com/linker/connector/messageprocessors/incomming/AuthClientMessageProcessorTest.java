package com.linker.connector.messageprocessors.incomming;

import com.google.common.collect.ImmutableMap;
import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.AuthClient;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestMessage;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import com.linker.connector.express.MockKafkaExpressDelivery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AuthClientMessageProcessorTest extends IntegrationTest {

    @Autowired
    MockKafkaExpressDelivery kafkaExpressDelivery;

    @Test
    public void test() {
        TestUser testUser = new TestUser("user1");
        testUser.onConnected(e -> {
            testUser.send(new TestMessage(MessageType.AUTH_CLIENT,
                    ImmutableMap.of(
                            "appId", "app-id-343",
                            "userId", "ANZ-123223",
                            "token", "token-12345"
                    ), MessageFeature.RELIABLE));

        });

        testUser.connect();
        Message actualMessage = kafkaExpressDelivery.getDeliveredMessage();
        Message expectedMessage = Message.builder()
                .content(
                        MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient("app-id-343", "ANZ-123223", "token-12345")
                                , MessageFeature.RELIABLE))
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L), "1"))
                .state(MessageState.CREATED)
                .build();

        TestUtils.messageEquals(expectedMessage, actualMessage);
    }
}
