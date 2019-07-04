package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.AuthClient;
import com.linker.connector.AuthStatus;
import com.linker.connector.IntegrationTest;
import com.linker.connector.NetworkUserService;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import com.linker.connector.express.MockKafkaExpressDelivery;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Slf4j
public class AuthClientMessageProcessorTest extends IntegrationTest {

    @Autowired
    MockKafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    NetworkUserService networkUserService;
    TestUser testUser;

    @After
    public void clean() throws InterruptedException, TimeoutException {
        if (testUser != null) {
            testUser.close();
            kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED);
        }
    }

    @Test
    public void testProcessor() throws InterruptedException {
        String userId = "ANZ-123223";
        assertEquals(0, networkUserService.getPendingUser(userId).size());
        testUser = TestUtils.connectClientUser(userId);
        Message actualMessage = kafkaExpressDelivery.getDeliveredMessage();
        Message expectedMessage = Message.builder()
                .content(
                        MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient("app-id-343", "ANZ-123223", "token-12345")
                                , MessageFeature.RELIABLE))
                .from(userId)
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L), "1"))
                .state(MessageState.CREATED)
                .build();

        TestUtils.messageEquals(expectedMessage, actualMessage);
        assertEquals(1, networkUserService.getPendingUser(userId).size());
        assertEquals(AuthStatus.AUTHENTICATING, networkUserService.getPendingUser(userId).get(0).getAuthStatus());
    }
}
