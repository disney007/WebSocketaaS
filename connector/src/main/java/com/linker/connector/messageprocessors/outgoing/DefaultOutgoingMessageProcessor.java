package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageType;
import com.linker.connector.NetworkUserService;
import com.linker.connector.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DefaultOutgoingMessageProcessor extends OutgoingMessageProcessor<Object> {
    @Autowired
    NetworkUserService networkUserService;

    @Override
    public MessageType getMessageType() {
        return MessageType.ANY;
    }

    @Override
    public void doProcess(Message message, Object data, MessageContext context) throws IOException {
        Address targetAddress = message.getMeta().getTargetAddress();
        SocketHandler handler = networkUserService.getUser(message.getTo(), targetAddress.getSocketId());
        if (handler != null) {
            handler.sendMessage(message);
        } else {
            log.warn("user [{}] not found on {} {}", message.getTo(), targetAddress.getDomainName(), targetAddress.getConnectorName());
        }
    }
}
