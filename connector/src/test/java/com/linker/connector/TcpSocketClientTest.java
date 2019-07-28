package com.linker.connector;

import com.google.common.collect.ImmutableMap;
import com.linker.common.*;
import com.linker.common.messages.AuthClient;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.network.TcpSocketClient;
import com.linker.connector.configurations.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Slf4j
public class TcpSocketClientTest extends IntegrationTest {

    @Autowired
    ApplicationConfig applicationConfig;

    TcpSocketClient socketClient;

    @Before
    public void setup() {
        socketClient = new TcpSocketClient();
    }

    @After
    public void clean() {
        socketClient.close();
    }

    @Test
    public void test() throws TimeoutException, ExecutionException, InterruptedException {
        TcpSocketClient socketClient = new TcpSocketClient();
        socketClient.connect("localhost", applicationConfig.getTcpPort());

        socketClient.onConnected(() -> {
            socketClient.sendMessage(new MessageContent(MessageType.AUTH_CLIENT,
                    ImmutableMap.of(
                            "appId", "app-id-343",
                            "userId", "ANZ-123223",
                            "token", "token-12345"
                    ), null, MessageFeature.RELIABLE, true));
        });

        CompletableFuture<MessageContentOutput> future = new CompletableFuture<>();
        socketClient.onMessage(future::complete);

        Message actualMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT);
        Message expectedMessage = Message.builder()
                .content(
                        MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient("app-id-343", "ANZ-123223", "token-12345")
                                , MessageFeature.RELIABLE))
                .from("ANZ-123223")
                .meta(new MessageMeta(new Address("domain-01", "connector-01", 1L), "1"))
                .state(MessageState.CREATED)
                .build();

        TestUtils.messageEquals(expectedMessage, actualMessage);

        String note = actualMessage.getMeta().getNote();
        String json = "{\"id\":\"db04b50b-9036-4e08-8ba8-1c4978f40833\",\"version\":\"0.1.0\",\"content\":{\"type\":\"AUTH_CLIENT_REPLY\",\"data\":{\"appId\":\"app-id-343\",\"userId\":\"ANZ-123223\",\"isAuthenticated\":true},\"feature\":\"RELIABLE\"},\"from\":\"SYSTEM\",\"to\":\"connector-01\",\"meta\":{\"originalAddress\":{\"domainName\":\"domain-01\"},\"targetAddress\":{\"domainName\":\"domain-01\",\"connectorName\":\"connector-01\"},\"note\":\"" + note + "\",\"ttl\":9},\"createdAt\":1562209615308,\"state\":\"CREATED\"}";
        kafkaExpressDelivery.onMessageArrived(json.getBytes(StandardCharsets.UTF_8));
        kafkaExpressDelivery.getDeliveredMessage(MessageType.USER_CONNECTED);

        MessageContentOutput actualMsgContent = future.get(3000L, TimeUnit.MILLISECONDS);
        actualMsgContent.setData(actualMsgContent.getData(AuthClientReply.class));
        MessageContentOutput expectedMsgContent = new MessageContentOutput(MessageType.AUTH_CLIENT_REPLY, new AuthClientReply(
                "app-id-343", "ANZ-123223", true
        ), null);
        assertEquals(expectedMsgContent, actualMsgContent);
    }
}
