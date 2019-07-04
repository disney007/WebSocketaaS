package com.linker.connector;

import com.linker.common.Utils;
import com.linker.connector.express.MockKafkaExpressDelivery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
@Slf4j
public abstract class IntegrationTest {

    @Autowired
    IntegrationTestEnv integrationTestEnv;

    @Autowired
    NetworkUserService userService;

    @Autowired
    WebSocketChannelInitializer webSocketChannelInitializer;

    @Autowired
    MockKafkaExpressDelivery kafkaExpressDelivery;

    @Before
    public void integrationSetup() {
        log.info("init integration env");
        integrationTestEnv.waitForReady();
        closeAllUsers(userService.pendingUsers);
        closeAllUsers(userService.users);
        webSocketChannelInitializer.resetCounter();
        kafkaExpressDelivery.reset();
        Utils.sleep(30L);
    }

    void closeAllUsers(ConcurrentHashMap<String, List<SocketHandler>> map) {
        map.forEach((key, value) -> value.forEach(SocketHandler::close));
    }

    protected static class AsyncStep {
        CompletableFuture<AsyncStep> future;

        public AsyncStep(CompletableFuture<AsyncStep> future) {
            this.future = future;
        }

        public void done() {
            this.future.complete(null);
        }
    }

    protected void async(Consumer<AsyncStep> consumer) {
        CompletableFuture<AsyncStep> future = new CompletableFuture<>();
        AsyncStep asyncTest = new AsyncStep(future);
        consumer.accept(asyncTest);

        try {
            future.get(30L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
