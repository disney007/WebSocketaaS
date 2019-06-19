package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class OutgoingMessageProcessor<T> extends MessageProcessor<T> {

}
