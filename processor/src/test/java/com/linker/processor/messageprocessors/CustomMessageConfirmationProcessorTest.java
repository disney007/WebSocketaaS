package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.MessageConfirmation;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUser;
import com.linker.processor.TestUtils;
import com.linker.processor.configurations.ApplicationConfig;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

public class CustomMessageConfirmationProcessorTest extends IntegrationTest {

    @Autowired
    MessageProcessorService messageProcessorService;

    @Autowired
    ApplicationConfig applicationConfig;

    @Test
    public void test_processing() throws TimeoutException {
        TestUser testUser = TestUtils.loginUser("ANZ-12345");
        Message confirmMessage = Message.builder()
                .from(Keywords.SYSTEM)
                .to("ANZ-12345")
                .content(MessageUtils.createMessageContent(MessageType.MESSAGE_CONFIRMATION, new MessageConfirmation("abc123"), MessageFeature.RELIABLE))
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName())))
                .build();

        messageProcessorService.process(confirmMessage);

        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.MESSAGE_CONFIRMATION);
        Message expectedDeliveredMessage = confirmMessage.clone();
        expectedDeliveredMessage.getMeta().setTargetAddress(testUser.getAddress());
        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);
    }
}
