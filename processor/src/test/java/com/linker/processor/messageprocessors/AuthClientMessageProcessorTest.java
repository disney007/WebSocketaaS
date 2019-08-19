package com.linker.processor.messageprocessors;

import com.google.common.collect.ImmutableMap;
import com.linker.common.*;
import com.linker.common.messages.AuthClient;
import com.linker.common.messages.AuthClientReply;
import com.linker.processor.IntegrationTest;
import com.linker.processor.TestUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.common.client.ClientApp;
import com.linker.processor.services.ClientAppService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AuthClientMessageProcessorTest extends IntegrationTest {

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    ClientAppService clientAppService;

    @Before
    @After
    public void reset() throws IOException {
        clientAppService.init();
    }

    @Test
    public void test_basic_auth() throws TimeoutException {
        testAuth("app-id-343", "ANZ-123223", true);
        testAuth("app-id-344", "ANZ-123223", false);
        testAuth("app-id-343", "ANZXX-123223", false);
        testAuth("app-id-343", "ANZ-3123223", false);
    }

    @Test
    public void test_external_auth_wrong_url() throws TimeoutException {
        ClientApp clientApp = clientAppService.getClientAppByName("ANZ");
        clientApp.setAuthEnabled(true);
        clientApp.setAuthUrl("");
        clientAppService.saveClientApp(clientApp);
        testAuth("app-id-343", "ANZ-123223", false);
    }

    @Test
    public void test_external_url() throws IOException, TimeoutException {
        do_test_external_url(new AuthClientReply("app-id-343", "ANZ-123223", true), true);
        do_test_external_url(new AuthClientReply("app-id-343", "ANZ-123223", false), false);
        do_test_external_url(ImmutableMap.of("some", "wrong"), false);
    }

    void do_test_external_url(Object response, boolean isAuthenticated) throws IOException, TimeoutException {
        MockWebServer server = new MockWebServer();
        try {

            server.enqueue(new MockResponse().setBody(Utils.toJson(response)));
            server.start();

            ClientApp clientApp = clientAppService.getClientAppByName("ANZ");
            clientApp.setAuthEnabled(true);
            clientApp.setAuthUrl(server.url("/auth").url().toString());
            clientAppService.saveClientApp(clientApp);
            testAuth("app-id-343", "ANZ-123223", isAuthenticated);
        } finally {
            server.shutdown();
        }
    }

    public void testAuth(String appId, String userId, boolean isAuthenticated) throws TimeoutException {
        Message message = Message.builder()
                .from(userId)
                .content(MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient(appId, userId, "abc"), MessageFeature.RELIABLE))
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), "connector-01"), null, "1", 10))
                .build();

        givenMessage(message);

        Message deliveredMessage = kafkaExpressDelivery.getDeliveredMessage(MessageType.AUTH_CLIENT_REPLY);

        Message expectedDeliveredMessage = Message.builder()
                .from(Keywords.SYSTEM)
                .to("connector-01")
                .content(MessageUtils.createMessageContent(MessageType.AUTH_CLIENT_REPLY, new AuthClientReply(appId, userId, isAuthenticated), MessageFeature.RELIABLE))
                .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName()),
                        new Address(applicationConfig.getDomainName(), "connector-01"), "1", 10))
                .build();

        TestUtils.messageEquals(expectedDeliveredMessage, deliveredMessage);
    }
}
