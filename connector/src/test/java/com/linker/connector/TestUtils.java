package com.linker.connector;

import com.google.common.collect.ImmutableMap;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageFeature;
import com.linker.common.MessageType;
import com.linker.common.Utils;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    public static void messageEquals(Message expectedMsg, Message actualMsg) {
        Object actualData = Utils.convert(actualMsg.getContent().getData(), expectedMsg.getContent().getData().getClass());
        actualMsg.getContent().setData(actualData);

        assertEquals(expectedMsg.getVersion(), actualMsg.getVersion());
        assertEquals(expectedMsg.getContent(), actualMsg.getContent());
        assertEquals(expectedMsg.getFrom(), actualMsg.getFrom());
        assertEquals(expectedMsg.getTo(), actualMsg.getTo());
        assertEquals(expectedMsg.getMeta(), actualMsg.getMeta());
        assertEquals(expectedMsg.getState(), actualMsg.getState());
    }

    public static TestUser connectClientUser(String userId) {
        TestUser testUser = new TestUser(userId);
        testUser.onConnected(e -> {
            testUser.send(new MessageContent(MessageType.AUTH_CLIENT,
                    ImmutableMap.of(
                            "appId", "app-id-343",
                            "userId", userId,
                            "token", "token-12345"
                    ), null, MessageFeature.RELIABLE));

        });

        testUser.connect();
        return testUser;
    }

}
