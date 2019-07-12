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

@Slf4j
public class CustomMessageProcessorTest extends IntegrationTest {

    @Test
    public void test_reliable_receiverNotFound() throws JsonProcessingException {
        Message incomingMessage = createReliableMessage();

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
        Message incomingMessage = createReliableMessage();
        kafkaExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE);

        Message expectedMessage = incomingMessage.clone();
        expectedMessage.setTo(testUser.getUserId());
        expectedMessage.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageForward("ANZ-123223", "some thing here to send"), MessageFeature.RELIABLE));
        expectedMessage.getMeta().setTargetAddress(testUser.getAddress());
        MessageUtils.touchMessage(expectedMessage);

        TestUtils.messageEquals(expectedMessage, deliveredMessage);
    }

    Message createReliableMessage() {
        return Message.builder()
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE, new MessageRequest("ANZ-1232122", "some thing here to send"), MessageFeature.RELIABLE))
                .build();
    }
}
