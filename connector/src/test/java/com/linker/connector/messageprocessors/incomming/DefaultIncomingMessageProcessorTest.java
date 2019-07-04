package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageType;
import com.linker.common.messages.MessageRequest;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.express.MockKafkaExpressDelivery;
import com.linker.connector.express.MockNatsExpressDelivery;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

@Slf4j
public class DefaultIncomingMessageProcessorTest extends IntegrationTest {

    TestUser testUser;
    String userId = "ANZ-123223";

    @Autowired
    MockKafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    MockNatsExpressDelivery natsExpressDelivery;

    @Autowired
    ApplicationConfig applicationConfig;

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser);
    }

    @Test
    public void testProcessor_reliable() throws TimeoutException {
        testUser = TestUtils.loginClientUser(userId);
        MessageContent testMessage = new MessageContent(MessageType.MESSAGE, new MessageRequest("target-user", "hello world"), "abc", MessageFeature.RELIABLE);
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
        MessageContent testMessage = new MessageContent(MessageType.MESSAGE, new MessageRequest("target-user", "hello world"), "abc", MessageFeature.FAST);
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
