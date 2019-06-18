package com.linker.common;


import java.util.HashMap;

public class MessageContext extends HashMap<String, Object> {
    public <T> T getValue(String key) {
        return (T) super.get(key);
    }
}
