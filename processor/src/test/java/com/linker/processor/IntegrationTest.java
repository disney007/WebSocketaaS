package com.linker.processor;

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

    @Before
    public void cleanDb() {
        messageRepository.removeAll();
        userChannelRepository.deleteAll();
    }
}

