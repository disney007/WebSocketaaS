package com.linker.processor.messageprocessors;

import com.linker.processor.IntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CustomMessageProcessorTest extends IntegrationTest {

    @Test
    public void test() {
        String incomingMessage = "{\"id\":\"1b386701-b658-43ae-a54c-01e33a27365b\",\"version\":\"0.1.0\",\"content\":{\"type\":\"MESSAGE\",\"data\":{\"to\":\"ANZ-1232122\",\"content\":\"some thing here to send\"},\"feature\":\"RELIABLE\",\"confirmationEnabled\":true},\"from\":\"ANZ-123223\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\",\"socketId\":1},\"ttl\":10},\"createdAt\":1562873695862,\"state\":\"CREATED\"}";

        kafkaExpressDelivery.onMessageArrived(incomingMessage);
        log.info("=== test finished ===");
    }
}
