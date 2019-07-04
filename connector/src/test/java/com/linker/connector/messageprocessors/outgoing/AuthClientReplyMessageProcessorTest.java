package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageResult;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.ResultStatus;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.messages.UserConnected;
import com.linker.connector.AuthStatus;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Slf4j
public class AuthClientReplyMessageProcessorTest extends IntegrationTest {

    String userId = "ANZ-123223";
    TestUser testUser;

    @After
    public void clean() throws TimeoutException {
        if (testUser != null) {
            testUser.close();
            kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED);
        }
    }

    @Test
    public void test_authenticated() throws TimeoutException {
        assertEquals(0, networkUserService.getUser(userId).size());
        testUser = TestUtils.connectClientUser(userId);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        kafkaExpressDelivery.onMessageArrived("{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"ANZ-123223\",\"token\":\"token-12345\",\"result\":{\"status\":\"OK\"}},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"1\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}");
        Message userConnectedMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_CONNECTED);
        checkUserConnectedMessage(userConnectedMessage);
        MessageContent receivedMessage = testUser.getReceivedMessage(MessageType.AUTH_CLIENT_REPLY);
        AuthClientReply data = receivedMessage.getData(AuthClientReply.class);
        checkAuthClientReply(data, MessageResult.ok());
        assertEquals(1, networkUserService.getUser(userId).size());
        assertEquals(AuthStatus.AUTHENTICATED, networkUserService.getUser(userId).get(0).getAuthStatus());
    }

    @Test
    public void test_not_authenticated() throws TimeoutException {
        assertEquals(0, networkUserService.getUser(userId).size());
        assertEquals(0, networkUserService.getPendingUser(userId).size());
        testUser = TestUtils.connectClientUser(userId);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        assertEquals(1, networkUserService.getPendingUser(userId).size());
        assertEquals(AuthStatus.AUTHENTICATING, networkUserService.getPendingUser(userId).get(0).getAuthStatus());
        kafkaExpressDelivery.onMessageArrived("{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"ANZ-123223\",\"token\":\"token-12345\",\"result\":{\"status\":\"FAILED\",\"message\":\"user not found\"}},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"1\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}");
        MessageContent receivedMessage = testUser.getReceivedMessage(MessageType.AUTH_CLIENT_REPLY);
        AuthClientReply data = receivedMessage.getData(AuthClientReply.class);
        checkAuthClientReply(data, new MessageResult(ResultStatus.FAILED, "user not found"));
        assertEquals(0, networkUserService.getPendingUser(userId).size());
        assertEquals(0, networkUserService.getUser(userId).size());
        testUser = null;
    }

    void checkAuthClientReply(AuthClientReply data, MessageResult authResult) {
        AuthClientReply reply = new AuthClientReply();
        reply.setResult(authResult);
        reply.setAppId("app-id-343");
        reply.setUserId(userId);
        reply.setToken("token-12345");
        assertEquals(reply, data);

    }

    void checkUserConnectedMessage(Message message) {
        MessageMeta meta = new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L));
        Message expectedMessage = Message.builder()
                .content(
                        MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnected(userId),
                                MessageFeature.RELIABLE)
                )
                .from(Keywords.SYSTEM)
                .meta(meta)
                .build();

        TestUtils.messageEquals(expectedMessage, message);
    }
}
