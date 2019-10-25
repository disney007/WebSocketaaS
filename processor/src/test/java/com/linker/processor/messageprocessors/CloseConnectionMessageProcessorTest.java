package com.linker.processor.messageprocessors;


import com.linker.common.*;
import com.linker.common.messages.CloseConnection;
import com.linker.common.messages.EmptyMessage;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import com.linker.processor.configurations.ApplicationConfig;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

public class CloseConnectionMessageProcessorTest extends IntegrationTest {

    @Autowired
    CloseConnectionMessageProcessor closeConnectionMessageProcessor;

    @Autowired
    ApplicationConfig applicationConfig;

    Message createMessage(String from, String to) {
        return Message.builder()
                .from(from)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), null, -1L)))
                .content(MessageUtils.createMessageContent(MessageType.CLOSE_CONNECTION, new CloseConnection(to), MessageFeature.FAST))
                .build();
    }

    @Test
    public void test_closeConnection_successfully() throws TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-123224");
        Message incomingMessage = createMessage("ANZ-123223", "ANZ-123224");
        givenMessage(incomingMessage);
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.CLOSE_CONNECTION);
        incomingMessage.setContent(MessageUtils.createMessageContent(MessageType.CLOSE_CONNECTION, new EmptyMessage(), MessageFeature.RELIABLE));
        incomingMessage.setTo("ANZ-123224");
        incomingMessage.getMeta().setTargetAddress(testUser.getAddress());
        MessageUtils.touchMessage(incomingMessage);

        TestUtils.messageEquals(incomingMessage, deliveredMessage);
    }

    @Test(expected = TimeoutException.class)
    public void test_closeConnection_noPermission() throws TimeoutException {
        TestUtils.loginUser("ANZ-123224");
        Message incomingMessage = createMessage("ANZ-123226", "ANZ-123224");
        givenMessage(incomingMessage);
        kafkaExpressDelivery.getDeliveredMessage(MessageType.CLOSE_CONNECTION);
    }
}
