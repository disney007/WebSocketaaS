package com.linker.connector.messageprocessors.outgoing;


import com.linker.common.*;
import com.linker.common.messages.EmptyMessage;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertTrue;

public class CloseConnectionMessageProcessorTest extends IntegrationTest {
    TestUser testUser;

    @Before
    public void setup() throws TimeoutException {
        testUser = TestUtils.loginClientUser("ANZ-123224");
    }

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser);
    }

    @Test
    public void test_closeConnection_successfully() throws TimeoutException {
        Message message = Message.builder()
                .from("ANZ-123223")
                .to("ANZ-123224")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 999L), new Address("domain-01", "connector-01", testUser.getSocketId())))
                .content(MessageUtils.createMessageContent(MessageType.CLOSE_CONNECTION, new EmptyMessage(), MessageFeature.RELIABLE))
                .build();
        givenMessage(message);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_DISCONNECTED);
        assertTrue(testUser.isClosed());
    }
}

