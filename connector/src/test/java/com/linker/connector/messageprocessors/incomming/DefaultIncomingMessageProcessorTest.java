package com.linker.connector.messageprocessors.incomming;

import com.linker.common.*;
import com.linker.common.messages.MessageRequest;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

@Slf4j
public class DefaultIncomingMessageProcessorTest extends IntegrationTest {

    TestUser testUser;
    String userId = "ANZ-123223";

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser);
    }

    @Test
    public void testProcessor_reliable() throws TimeoutException {
        testUser = TestUtils.loginClientUser(userId);
        MessageContent testMessage = new MessageContent(MessageType.MESSAGE, new MessageRequest("target-user", "hello world"), "abc", MessageFeature.RELIABLE, true);
        testUser.send(testMessage);
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);

        Message expectedMessage = Message.builder()
                .content(testMessage)
                .from(this.userId)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)))
                .build();

        TestUtils.messageEquals(expectedMessage, deliveredMessage);
    }

    @Test
    public void testProcessor_fast() throws TimeoutException {
        testUser = TestUtils.loginClientUser(userId);
        MessageContent testMessage = new MessageContent(MessageType.MESSAGE, new MessageRequest("target-user", "hello world"), "abc", MessageFeature.FAST, true);
        testUser.send(testMessage);
        Message deliveredMessage = natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);

        Message expectedMessage = Message.builder()
                .content(testMessage)
                .from(this.userId)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)))
                .build();

        TestUtils.messageEquals(expectedMessage, deliveredMessage);
    }
}
