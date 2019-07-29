package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.UserConnected;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import com.linker.processor.models.ClientApp;
import com.linker.processor.models.UserChannel;
import com.linker.processor.services.ClientAppService;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


@Slf4j
public class UserConnectedMessageProcessorTest extends IntegrationTest {

    @Autowired
    ClientAppService clientAppService;

    @Autowired
    UserChannelService userChannelService;

    @Test
    public void test_normal_user_connected_processing() throws TimeoutException {
        ClientApp clientApp = clientAppService.getClientAppByName("ANZ");
        String masterUserId = clientApp.getMasterUserId();
        TestUser masterUser = TestUtils.loginUser(masterUserId, 11L);

        String userId = "ANZ-12345";
        TestUser testUser = TestUtils.loginUser(userId, 10L);
        UserChannel user = userChannelService.getById(userId);
        assertEquals(testUser.getAddress(), user.getAddresses().iterator().next());

        // check message sent to master
        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_CONNECTED);
        Message expectedDeliveredMessage =
                MessageUtils.touchMessage(
                        Message.builder()
                                .from(Keywords.SYSTEM)
                                .to(masterUserId)
                                .content(MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnected(userId), MessageFeature.RELIABLE))
                                .meta(new MessageMeta(testUser.getAddress(), masterUser.getAddress()))
                                .build()
                );
        expectedDeliveredMessage.getMeta().setDeliveryType(DeliveryType.ANY);
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);

        // check saved message
        Message savedMessage = messageRepository.findById(deliveredMessage.getId());
        Message expectedSavedMessage = deliveredMessage.clone();
        expectedSavedMessage.setTo(null);
        expectedSavedMessage.getMeta().setTargetAddress(null);
        TestUtils.messageEquals(expectedSavedMessage, savedMessage);
    }

    @Test
    public void test_master_user_connected_processing() {
        ClientApp clientApp = clientAppService.getClientAppByName("ANZ");
        String masterUserId = clientApp.getMasterUserId();
        TestUser masterUser = TestUtils.loginUser(masterUserId, 11L);
        Message connectedMessage = messageRepository.findById(masterUser.getConnectedMessageId());
        assertEquals(MessageState.PROCESSED, connectedMessage.getState());
    }
}
