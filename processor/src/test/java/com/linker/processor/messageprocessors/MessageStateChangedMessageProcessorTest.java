package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageConfirmation;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.common.messages.MessageStateChanged;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class MessageStateChangedMessageProcessorTest extends IntegrationTest {

    @Test
    public void test_status_changed() {
        status_changed(MessageState.PROCESSED, false);
        status_changed(MessageState.NETWORK_ERROR, true);
        status_changed(MessageState.ADDRESS_NOT_FOUND, true);
    }

    @Test
    public void test_message_confirmation() throws TimeoutException {
        TestUtils.loginUser("ANZ-123223", new Address("domain-01", "connector-01", 11L));
        status_changed(MessageState.PROCESSED, false);

        // check delivered message;
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE_CONFIRMATION);
        assertEquals("abc123", deliveredMessage.getContent().toContentOutput().getData(MessageConfirmation.class).getReference());
        assertEquals("ANZ-123223", deliveredMessage.getTo());
    }

    public void status_changed(MessageState state, boolean persisted) {
        String messageId = UUID.randomUUID().toString();
        Message originalMessage = Message.builder()
                .id(messageId)
                .from("ANZ-123223")
                .to("ANZ-12345")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L), new Address("domain-01", "connector-01", 2L)))
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), "abc123", MessageFeature.RELIABLE))
                .build();

        givenMessage(
                Message.builder()
                        .from(Keywords.SYSTEM)
                        .to(Keywords.PROCESSOR)
                        .meta(new MessageMeta(new Address("Domain-01", "Connector-01")))
                        .content(MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(originalMessage, state), MessageFeature.RELIABLE))
                        .build()
        );

        Message dbMessage = messageRepository.findById(messageId);
        if (persisted) {
            assertEquals(state, dbMessage.getState());
        } else {
            assertNull(dbMessage);
        }

    }
}
