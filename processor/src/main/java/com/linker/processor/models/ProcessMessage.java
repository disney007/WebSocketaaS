package com.linker.processor.models;

import com.linker.common.MessageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProcessMessage {
    MessageType messageType();

    Class<?> messageModel();
}
