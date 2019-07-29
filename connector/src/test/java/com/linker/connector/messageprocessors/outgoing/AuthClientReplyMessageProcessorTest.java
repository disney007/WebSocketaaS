package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.*;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.messages.UserConnected;
import com.linker.connector.AuthStatus;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
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
    public void test_authenticated() throws TimeoutException, IOException {
        assertEquals(0, networkUserService.getUser(userId).size());
        testUser = TestUtils.connectClientUser(userId);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        String json = "{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"ANZ-123223\",\"isAuthenticated\":true},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"1\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}";
        givenMessage(Utils.fromJson(json, Message.class));
        Message userConnectedMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_CONNECTED);
        checkUserConnectedMessage(userConnectedMessage);
        MessageContentOutput receivedMessage = testUser.getReceivedMessage(MessageType.AUTH_CLIENT_REPLY);
        AuthClientReply data = receivedMessage.getData(AuthClientReply.class);
        checkAuthClientReply(data, true);
        assertEquals(1, networkUserService.getUser(userId).size());
        assertEquals(AuthStatus.AUTHENTICATED, networkUserService.getUser(userId).get(0).getAuthStatus());
    }

    @Test
    public void test_not_authenticated() throws TimeoutException, IOException {
        assertEquals(0, networkUserService.getUser(userId).size());
        assertEquals(0, networkUserService.getPendingUser(userId).size());
        testUser = TestUtils.connectClientUser(userId);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        assertEquals(1, networkUserService.getPendingUser(userId).size());
        assertEquals(AuthStatus.AUTHENTICATING, networkUserService.getPendingUser(userId).get(0).getAuthStatus());
        String json = "{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"ANZ-123223\",\"isAuthenticated\":false},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"1\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}";
        givenMessage(Utils.fromJson(json, Message.class));
        MessageContentOutput receivedMessage = testUser.getReceivedMessage(MessageType.AUTH_CLIENT_REPLY);
        AuthClientReply data = receivedMessage.getData(AuthClientReply.class);
        checkAuthClientReply(data, false);
        assertEquals(0, networkUserService.getPendingUser(userId).size());
        assertEquals(0, networkUserService.getUser(userId).size());
        testUser = null;
    }

    void checkAuthClientReply(AuthClientReply data, Boolean authResult) {
        AuthClientReply reply = new AuthClientReply();
        reply.setIsAuthenticated(authResult);
        reply.setAppId("app-id-343");
        reply.setUserId(userId);
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
