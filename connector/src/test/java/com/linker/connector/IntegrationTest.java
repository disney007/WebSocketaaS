package com.linker.connector;

import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.Utils;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
import com.linker.connector.configurations.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
@Slf4j
public abstract class IntegrationTest {

    @Autowired
    protected IntegrationTestEnv integrationTestEnv;

    @Autowired
    protected NetworkUserService networkUserService;

    @Autowired
    protected WebSocketChannelInitializer webSocketChannelInitializer;

    @Autowired
    protected MockKafkaExpressDelivery kafkaExpressDelivery;

    @Autowired
    protected MockNatsExpressDelivery natsExpressDelivery;

    @Autowired
    protected ApplicationConfig applicationConfig;

    @Autowired
    protected Codec codec;

    @Before
    public void integrationSetup() {
        log.info("init integration env");
        integrationTestEnv.waitForReady();
        closeAllUsers(networkUserService.pendingUsers);
        closeAllUsers(networkUserService.users);
        webSocketChannelInitializer.resetCounter();
        kafkaExpressDelivery.reset();
        natsExpressDelivery.reset();
        Utils.sleep(30L);
    }

    void closeAllUsers(ConcurrentHashMap<String, List<SocketHandler>> map) {
        map.forEach((key, value) -> value.forEach(SocketHandler::close));
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
