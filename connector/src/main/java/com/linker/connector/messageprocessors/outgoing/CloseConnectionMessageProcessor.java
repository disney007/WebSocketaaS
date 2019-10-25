package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageType;
import com.linker.common.messages.EmptyMessage;
import com.linker.connector.NetworkUserService;
import com.linker.connector.network.SocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloseConnectionMessageProcessor extends OutgoingMessageProcessor<EmptyMessage> {
    final NetworkUserService networkUserService;

    @Override
    public MessageType getMessageType() {
        return MessageType.CLOSE_CONNECTION;
    }

    @Override
    public void doProcess(Message message, EmptyMessage data, MessageContext context) {
        String userId = message.getTo();
        Long socketId = message.getMeta().getTargetAddress().getSocketId();
        SocketHandler handler = networkUserService.getUser(userId, socketId);
        if (handler != null) {
            log.info("close connection for userId [{}], socketId [{}]", userId, socketId);
            handler.close();
        }
    }
}
