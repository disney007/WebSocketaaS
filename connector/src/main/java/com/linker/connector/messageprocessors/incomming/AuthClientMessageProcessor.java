package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.models.AuthClientMessage;
import com.linker.connector.PostOffice;
import com.linker.connector.NetworkUserService;
import com.linker.connector.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class AuthClientMessageProcessor extends IncomingMessageProcessor<AuthClientMessage> {

    @Autowired
    NetworkUserService networkUserService;

    @Autowired
    PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT;
    }

    public AuthClientMessageProcessor() {

    }

    @Override
    public void doProcess(Message message, AuthClientMessage data, SocketHandler socketHandler) throws IOException {
        String userId = data.getUserId();
        message.setFrom(userId);
        message.getMeta().setNote(UUID.randomUUID().toString());
        socketHandler.setUserId(userId);
        networkUserService.addPendingUser(userId, socketHandler);
        this.postOffice.deliveryMessage(message);
    }
}
