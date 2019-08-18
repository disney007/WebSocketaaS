package com.linker.connector.messageprocessors.outgoing;

import com.google.common.collect.ImmutableSet;
import com.linker.common.*;
import com.linker.common.messages.GroupConnectorMessage;
import com.linker.common.messages.MessageForward;
import com.linker.common.models.ConnectorUserId;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;

@Slf4j
public class GroupConnectorMessageProcessorTest extends IntegrationTest {

    TestUser testUser1;
    TestUser testUser2;

    @Before
    public void setup() throws TimeoutException {
        testUser1 = TestUtils.loginClientUser("ANZ-123223");
        testUser2 = TestUtils.loginClientUser("ANZ-123224");
    }

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser1);
        TestUtils.logout(testUser2);
    }

    @Test
    public void testProcessing() throws TimeoutException {
        MessageForward messageData = new MessageForward("ANZ-123221", "hello world");
        givenMessage(
                Message.builder()
                        .from("ANZ-123221")
                        .to("Connector-01")
                        .content(MessageUtils.createMessageContent(MessageType.GROUP_CONNECTOR_MESSAGE, new GroupConnectorMessage(
                                ImmutableSet.of(new ConnectorUserId(testUser1.getUsername(), testUser1.getSocketId()),
                                        new ConnectorUserId(testUser2.getUsername(), testUser2.getSocketId()))
                                , messageData
                        ), MessageFeature.RELIABLE))
                        .meta(new MessageMeta(new Address("domain-01", "connector-01", 10L)))
                        .build()
        );
        assertEquals(messageData, testUser1.getReceivedMessage(MessageType.MESSAGE).getData(MessageForward.class));
        assertEquals(messageData, testUser2.getReceivedMessage(MessageType.MESSAGE).getData(MessageForward.class));
    }
}
