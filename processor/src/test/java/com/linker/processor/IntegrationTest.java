package com.linker.processor;

import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
import com.linker.processor.express.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
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
    TestConfig testConfig;

    static IntegrationTest instance;

    public IntegrationTest() {
        instance = this;
    }

    @AfterClass
    public static void clean() {
        log.info("start cleaning");
        instance.testConfig.clean();
    }
}

