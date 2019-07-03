package com.linker.connector;

import com.linker.common.Message;
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
}
