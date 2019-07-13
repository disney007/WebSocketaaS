package com.linker.processor.messageprocessors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.Utils;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNull;

@Slf4j
public class CustomMessageProcessorTest extends IntegrationTest {

    @Test
    public void test_reliable_receiverNotFound() throws JsonProcessingException {
        Message incomingMessage = createMessage(MessageFeature.RELIABLE);

        kafkaExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));
        Message savedMessage = messageRepository.findById(incomingMessage.getId());

        Message expectedMessage = incomingMessage.clone();
        expectedMessage.setTo("ANZ-1232122");
        expectedMessage.setState(MessageState.TARGET_NOT_FOUND);
        MessageUtils.touchMessage(expectedMessage);

        TestUtils.messageEquals(expectedMessage, savedMessage);
    }

    @Test
    public void test_reliable_receiverFound() throws JsonProcessingException, TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-1232122");
        Message incomingMessage = createMessage(MessageFeature.RELIABLE);
        kafkaExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);

        Message expectedDeliveredMessage = incomingMessage.clone();
        expectedDeliveredMessage.setTo(testUser.getUserId());
        expectedDeliveredMessage.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.RELIABLE));
        expectedDeliveredMessage.getMeta().setTargetAddress(testUser.getAddress());
        MessageUtils.touchMessage(expectedDeliveredMessage);
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);

        Message savedMessage = messageRepository.findById(incomingMessage.getId());
        Message expectedSavedMessage = incomingMessage.clone();
        expectedSavedMessage.setTo("ANZ-1232122");
        expectedSavedMessage.setState(MessageState.CREATED);
        MessageUtils.touchMessage(expectedSavedMessage);
        TestUtils.messageEquals(expectedSavedMessage, savedMessage);
    }

    @Test
    public void test_fast_receiverNotFound() throws JsonProcessingException {
        Message incomingMessage = createMessage(MessageFeature.FAST);
        natsExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));
        Message savedMessage = messageRepository.findById(incomingMessage.getId());
        assertNull(savedMessage);
    }

    @Test
    public void test_fast_receiverFound() throws JsonProcessingException, TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-1232122");
        Message incomingMessage = createMessage(MessageFeature.FAST);
        natsExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));

        Message deliveredMessage = natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);
        Message expectedDeliveredMessage = incomingMessage.clone();
        expectedDeliveredMessage.setTo(testUser.getUserId());
        expectedDeliveredMessage.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.FAST));
        expectedDeliveredMessage.getMeta().setTargetAddress(testUser.getAddress());
        MessageUtils.touchMessage(expectedDeliveredMessage);
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);

        Message savedMessage = messageRepository.findById(incomingMessage.getId());
        assertNull(savedMessage);
    }

    Message createMessage(MessageFeature feature) {
        return Message.builder()
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageRequest("ANZ-1232122", "some thing here to send"), feature))
                .build();
    }
}
