package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageConfirmation;
import com.linker.common.messages.MessageRequest;
import com.linker.common.messages.MessageStateChanged;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


public class MessageStateChangedMessageProcessorTest extends IntegrationTest {

    @Test
    public void test_status_changed() {
        status_changed(MessageState.PROCESSED);
        status_changed(MessageState.NETWORK_ERROR);
        status_changed(MessageState.TARGET_NOT_FOUND);
    }

    @Test
    public void test_message_confirmation() throws TimeoutException {
        TestUtils.loginUser("ANZ-123223", new Address("domain-01", "connector-01", 11L));
        status_changed(MessageState.PROCESSED);

        // check delivered message;
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE_CONFIRMATION);
        assertEquals("abc123", deliveredMessage.getContent().toContentOutput().getData(MessageConfirmation.class).getReference());
        assertEquals("ANZ-123223", deliveredMessage.getTo());
    }

    public void status_changed(MessageState state) {
        TestUser testUser = TestUtils.loginUser("ANZ-12345");
        String messageId = UUID.randomUUID().toString();
        Message incomingMessage = givenMessage(
                Message.builder()
                        .id(messageId)
                        .from("ANZ-123223")
                        .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                        .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageRequest("ANZ-12345", "some thing here to send"), "abc123", MessageFeature.RELIABLE))
                        .build()
        );

        assertEquals(MessageState.CREATED, messageRepository.findById(messageId).getState());

        Message stateChangedContentMsg = incomingMessage.toContentlessMessage();
        stateChangedContentMsg.setTo("ANZ-12345");
        givenMessage(
                Message.builder()
                        .from(Keywords.SYSTEM)
                        .meta(new MessageMeta(testUser.getAddress()))
                        .content(MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(stateChangedContentMsg, state), MessageFeature.RELIABLE))
                        .build()
        );

        assertEquals(state, messageRepository.findById(messageId).getState());
    }
}
