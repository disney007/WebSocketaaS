package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Slf4j
public class CustomMessageProcessorTest extends IntegrationTest {

    @Autowired
    CustomMessageProcessor customMessageProcessor;

    @Test
    public void test_reliable_receiverNotFound() {
        Message incomingMessage = createMessage(MessageFeature.RELIABLE);

        givenMessage(incomingMessage);
        Message savedMessage = messageRepository.findById(incomingMessage.getId());

        Message expectedMessage = Message.builder()
                .from("ANZ-123223")
                .to("ANZ-1232122")
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.RELIABLE))
                .state(MessageState.ADDRESS_NOT_FOUND)
                .meta(new MessageMeta(incomingMessage.getMeta().getOriginalAddress(), new Address("domain-01", null, -1L)))
                .build();

        MessageUtils.touchMessage(expectedMessage);
        TestUtils.messageEquals(expectedMessage, savedMessage);
    }

    @Test
    public void test_reliable_receiverFound() throws TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-1232122");
        Message incomingMessage = createMessage(MessageFeature.RELIABLE);
        givenMessage(incomingMessage);
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);

        Message expectedDeliveredMessage = incomingMessage.clone();
        expectedDeliveredMessage.setTo(testUser.getUserId());
        expectedDeliveredMessage.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.RELIABLE));
        expectedDeliveredMessage.getMeta().setTargetAddress(testUser.getAddress());
        MessageUtils.touchMessage(expectedDeliveredMessage);
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);
    }

    @Test
    public void test_fast_receiverNotFound() {
        Message incomingMessage = createMessage(MessageFeature.FAST);
        givenMessage(incomingMessage);
        Message savedMessage = messageRepository.findById(incomingMessage.getId());
        assertNull(savedMessage);
    }

    @Test
    public void test_fast_receiverFound() throws TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-1232122");
        Message incomingMessage = createMessage(MessageFeature.FAST);
        givenMessage(incomingMessage);

        Message deliveredMessage = natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);
        Message expectedDeliveredMessage = incomingMessage.clone();
        expectedDeliveredMessage.setTo(testUser.getUserId());
        expectedDeliveredMessage.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.FAST));
        expectedDeliveredMessage.getMeta().setTargetAddress(testUser.getAddress());
        expectedDeliveredMessage.getMeta().setConfirmEnabled(false);
        MessageUtils.touchMessage(expectedDeliveredMessage);
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);

        Message savedMessage = messageRepository.findById(incomingMessage.getId());
        assertNull(savedMessage);
    }

    @Test
    public void testUpdateMessageConfirmationFlag() {
        doTestUpdateMessageConfirmationFlag(MessageFeature.RELIABLE, "abc", true, true);
        doTestUpdateMessageConfirmationFlag(MessageFeature.RELIABLE, "", true, false);
        doTestUpdateMessageConfirmationFlag(MessageFeature.RELIABLE, "abc", false, false);
        doTestUpdateMessageConfirmationFlag(MessageFeature.RELIABLE, "", false, false);

        doTestUpdateMessageConfirmationFlag(MessageFeature.FAST, "abc", true, true);
        doTestUpdateMessageConfirmationFlag(MessageFeature.FAST, "", true, false);
        doTestUpdateMessageConfirmationFlag(MessageFeature.FAST, "abc", false, false);
        doTestUpdateMessageConfirmationFlag(MessageFeature.FAST, "", false, false);
    }

    public void doTestUpdateMessageConfirmationFlag(MessageFeature feature, String reference, boolean contentConfirmEnabled, boolean confirmEnabled) {
        Message message = createMessage(feature);
        message.getContent().setReference(reference);
        message.getContent().setConfirmationEnabled(contentConfirmEnabled);
        customMessageProcessor.updateMessageConfirmationFlag(message);
        assertEquals(confirmEnabled, message.getMeta().isConfirmEnabled());
    }

    Message createMessage(MessageFeature feature) {
        return Message.builder()
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageRequest("ANZ-1232122", "some thing here to send"), feature))
                .build();
    }
}
