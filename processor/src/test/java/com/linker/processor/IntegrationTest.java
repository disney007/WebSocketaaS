package com.linker.processor;

import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.repositories.UserChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = {TestConfig.class, Application.class})
@Slf4j
public abstract class IntegrationTest {

    @Autowired
    protected MockKafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    protected MockNatsExpressDelivery natsExpressDelivery;

    @Autowired
    protected PostOffice postOffice;

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    UserChannelRepository userChannelRepository;

    @Autowired
    Codec codec;

    @Before
    public void clean() {
        messageRepository.removeAll();
        userChannelRepository.deleteAll();
        kafkaExpressDelivery.reset();
        natsExpressDelivery.reset();
    }

    protected Message givenMessage(Message message) {
        byte[] msg = codec.serialize(message);
        if (message.getContent().getFeature() == MessageFeature.RELIABLE) {
            kafkaExpressDelivery.onMessageArrived(msg);
        } else {
            natsExpressDelivery.onMessageArrived(msg);
        }
        return message;
    }
}

