package com.linker.processor.messageprocessors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.Utils;
import com.linker.common.messages.GroupMessage;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageRequest;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CustomGroupMessageProcessorTest extends IntegrationTest {

    @Test
    public void test_reliable_processing() throws JsonProcessingException, TimeoutException {
        List<TestUser> users = ImmutableList.of(
                TestUtils.loginUser("ANZ-1232122", new Address("domain-01", "connector-01", 10L)),
                TestUtils.loginUser("ANZ-1232123", new Address("domain-01", "connector-01", 11L)),
                TestUtils.loginUser("ANZ-1232124", new Address("domain-01", "connector-01", 12L))
        );

        Message incomingMessage = createMessage(MessageFeature.RELIABLE);
        kafkaExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));

        // check delivered messages
        List<Message> deliveredMessages = Arrays.asList(
                kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE),
                kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE),
                kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE)
        );
        List<Message> expectedDeliveredMessages = users.stream().map(user -> {
            Message msg = incomingMessage.clone();
            msg.setTo(user.getUserId());
            msg.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE,
                    new MessageForward(incomingMessage.getFrom(), "some thing here to send"), MessageFeature.RELIABLE));
            msg.getContent().setConfirmationEnabled(false);
            msg.getMeta().setTargetAddress(user.getAddress());
            MessageUtils.touchMessage(msg, 2);
            return msg;
        }).collect(Collectors.toList());

        TestUtils.messagesEqual(expectedDeliveredMessages, deliveredMessages);

        // check saved messages
        List<Message> expectedSavedMessages = users.stream().map(user -> {
            Message msg = incomingMessage.clone();
            msg.setTo(user.getUserId());
            msg.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE,
                    new MessageRequest(user.getUserId(), "some thing here to send"), MessageFeature.RELIABLE));
            msg.getContent().setConfirmationEnabled(false);
            MessageUtils.touchMessage(msg, 2);
            return msg;
        }).collect(Collectors.toList());
        Message targetNotFoundMessage = Message.builder()
                .from(incomingMessage.getFrom())
                .to("ANZ-1232125")
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE,
                        new MessageRequest("ANZ-1232125", "some thing here to send"), MessageFeature.RELIABLE))
                .meta(new MessageMeta(incomingMessage.getMeta().getOriginalAddress()))
                .state(MessageState.TARGET_NOT_FOUND)
                .build();
        targetNotFoundMessage.getContent().setConfirmationEnabled(false);
        MessageUtils.touchMessage(targetNotFoundMessage, 2);
        expectedSavedMessages.add(targetNotFoundMessage);

        List<Message> savedMessages = messageRepository.findMessagesByTypes(ImmutableSet.of(MessageType.MESSAGE), 100)
                .get().collect(Collectors.toList());
        TestUtils.messagesEqual(expectedSavedMessages, savedMessages);
    }

    @Test
    public void test_fast_processing() throws JsonProcessingException, TimeoutException {
        List<TestUser> users = ImmutableList.of(
                TestUtils.loginUser("ANZ-1232122", new Address("domain-01", "connector-01", 10L)),
                TestUtils.loginUser("ANZ-1232123", new Address("domain-01", "connector-01", 11L)),
                TestUtils.loginUser("ANZ-1232124", new Address("domain-01", "connector-01", 12L))
        );

        Message incomingMessage = createMessage(MessageFeature.FAST);
        natsExpressDelivery.onMessageArrived(Utils.toJson(incomingMessage));

        // check delivered messages
        List<Message> deliveredMessages = Arrays.asList(
                natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE),
                natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE),
                natsExpressDelivery.getDeliveredMessage(MessageType.MESSAGE)
        );
        List<Message> expectedDeliveredMessages = users.stream().map(user -> {
            Message msg = incomingMessage.clone();
            msg.setTo(user.getUserId());
            msg.setContent(MessageUtils.createMessageContent(MessageType.MESSAGE,
                    new MessageForward(incomingMessage.getFrom(), "some thing here to send"), MessageFeature.FAST));
            msg.getContent().setConfirmationEnabled(false);
            msg.getMeta().setTargetAddress(user.getAddress());
            msg.getMeta().setConfirmEnabled(false);
            MessageUtils.touchMessage(msg, 2);
            return msg;
        }).collect(Collectors.toList());

        TestUtils.messagesEqual(expectedDeliveredMessages, deliveredMessages);

        // check saved messages
        assertEquals(0L, messageRepository.findMessagesByTypes(ImmutableSet.of(MessageType.MESSAGE), 100).getTotalElements());
    }

    Message createMessage(MessageFeature feature) {
        Set<String> users = ImmutableSet.of("ANZ-1232122", "ANZ-1232123", "ANZ-1232124", "ANZ-1232125");

        return Message.builder()
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L)))
                .content(MessageUtils.createMessageContent(MessageType.GROUP_MESSAGE,
                        new GroupMessage(users, "some thing here to send"), feature))
                .build();
    }
}