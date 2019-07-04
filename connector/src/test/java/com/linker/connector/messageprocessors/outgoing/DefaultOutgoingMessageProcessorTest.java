package com.linker.connector.messageprocessors.outgoing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.Utils;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageStateChanged;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


@Slf4j
public class DefaultOutgoingMessageProcessorTest extends IntegrationTest {

    TestUser testUser;
    String userId = "ANZ-123223";
    String userId2 = "ANZ-123224";

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser);
    }

    @Test
    public void test_successfullySentOut() throws TimeoutException, JsonProcessingException {
        testUser = TestUtils.loginClientUser(userId);
        MessageContent content = new MessageContent(MessageType.MESSAGE,
                new MessageForward(userId2, "hi, this is the message form some one"),
                null, MessageFeature.FAST
        );

        Message receivedMessage = Message.builder()
                .content(content)
                .from(userId2)
                .to(userId)
                .meta(new MessageMeta(
                        new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 2L),
                        new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)
                ))
                .build();

        natsExpressDelivery.onMessageArrived(Utils.toJson(receivedMessage));

        // check user received message
        MessageContent userReceivedMessage = testUser.getReceivedMessage(MessageType.MESSAGE);
        userReceivedMessage.setData(userReceivedMessage.getData(MessageForward.class));
        assertEquals(content, userReceivedMessage);

        // check confirmation message
        Message stateChangedMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE_STATE_CHANGED);
        Message expectedStateChangedMessage = Message.builder()
                .from(Keywords.SYSTEM)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)))
                .content(
                        MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(receivedMessage.toSnapshot(), MessageState.PROCESSED), MessageFeature.RELIABLE)
                )
                .build();
        TestUtils.messageEquals(expectedStateChangedMessage, stateChangedMessage);
    }
}
