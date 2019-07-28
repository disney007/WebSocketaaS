package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.*;
import com.linker.common.exceptions.UnwantedMessageException;
import com.linker.common.messages.MessageForward;
import com.linker.common.messages.MessageStateChanged;
import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import com.linker.connector.TestUtils;
import com.linker.connector.network.SocketHandler;
import io.netty.channel.DefaultChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@Slf4j
public class DefaultOutgoingMessageProcessorTest extends IntegrationTest {

    TestUser testUser;
    String userId1 = "ANZ-123223";
    String userId2 = "ANZ-123224";

    @After
    public void clean() throws TimeoutException {
        TestUtils.logout(testUser);
    }

    @Test
    public void test_successfullySentOut() throws TimeoutException {
        testUser = TestUtils.loginClientUser(userId1);
        Message receivedMessage = messageArrived();

        // check user received message
        MessageContentOutput userReceivedMessage = testUser.getReceivedMessage(MessageType.MESSAGE);
        userReceivedMessage.setData(userReceivedMessage.getData(MessageForward.class));
        assertEquals(receivedMessage.getContent().toContentOutput(), userReceivedMessage);

        // check confirmation message
        checkConfirmedMessage(receivedMessage, MessageState.PROCESSED);
    }

    @Test
    public void test_successfully_confirmDisabled_SentOut() throws TimeoutException, UnwantedMessageException {
        testUser = TestUtils.loginClientUser(userId1);
        Message receivedMessage = messageArrived(false);

        // check user received message
        MessageContentOutput userReceivedMessage = testUser.getReceivedMessage(MessageType.MESSAGE);
        userReceivedMessage.setData(userReceivedMessage.getData(MessageForward.class));
        assertEquals(receivedMessage.getContent().toContentOutput(), userReceivedMessage);

        // check confirmation message
        kafkaExpressDelivery.noDeliveredMessage(MessageType.MESSAGE_STATE_CHANGED);
    }

    @Test
    public void test_targetNotFound() throws TimeoutException {
        Message receivedMessage = messageArrived();
        // check confirmation message
        checkConfirmedMessage(receivedMessage, MessageState.TARGET_NOT_FOUND);
    }

    @Test
    public void test_errorInSendingMessage() throws TimeoutException {
        testUser = TestUtils.loginClientUser(userId1);
        List<SocketHandler> user = networkUserService.getUser(userId1);
        SocketHandler spySocketHandler = spy(user.get(0));
        DefaultChannelPromise channelFuture = new DefaultChannelPromise(spySocketHandler.getChannel());
        doReturn(channelFuture).when(spySocketHandler).sendMessage(any(Message.class));
        user.set(0, spySocketHandler);
        Message receivedMessage = messageArrived();
        channelFuture.setFailure(new RuntimeException());
        // check confirmation message
        checkConfirmedMessage(receivedMessage, MessageState.NETWORK_ERROR);
    }


    Message messageArrived() {
        return messageArrived(true);
    }

    Message messageArrived(boolean confirmEnabled) {
        MessageContent content = new MessageContent(MessageType.MESSAGE,
                new MessageForward(userId2, "hi, this is the message form some one"),
                null, MessageFeature.FAST, true
        );

        MessageMeta messageMeta = new MessageMeta(
                new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 2L),
                new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)
        );
        messageMeta.setConfirmEnabled(confirmEnabled);
        Message receivedMessage = Message.builder()
                .content(content)
                .from(userId2)
                .to(userId1)
                .meta(messageMeta)
                .build();

        return givenMessage(receivedMessage);
    }

    void checkConfirmedMessage(Message receivedMessage, MessageState state) throws TimeoutException {
        Message stateChangedMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE_STATE_CHANGED);
        Message expectedStateChangedMessage = Message.builder()
                .from(Keywords.SYSTEM)
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), 1L)))
                .content(
                        MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(receivedMessage.toContentlessMessage(), state), MessageFeature.RELIABLE)
                )
                .build();
        TestUtils.messageEquals(expectedStateChangedMessage, stateChangedMessage);
    }
}
