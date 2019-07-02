package com.linker.connector.messageprocessors.incomming;

import com.linker.connector.IntegrationTest;
import com.linker.connector.TestUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class AuthClientMessageProcessorTest extends IntegrationTest {

    @Test
    public void test() {
        asyncTest(test -> {
            TestUser testUser = new TestUser("user1");
            testUser.onConnected(e -> {
                log.info("======= connected =======");
                test.done();
            });
            testUser.onError(e -> {
                log.error("===== error ===", e);
            });
            testUser.onClosed(e -> {
                log.error("===== on closed ===", e);
            });
            testUser.connect();
        });
    }
}
