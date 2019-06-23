package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageType;
import com.linker.common.models.AuthClientMessage;
import com.linker.connector.AuthStatus;
import com.linker.connector.NetworkUserService;
import com.linker.connector.PostOffice;
import com.linker.connector.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
        if (socketHandler.getAuthStatus() == AuthStatus.NOT_AUTHENTICATED) {
            String userId = data.getUserId();
            message.setFrom(userId);
            message.getMeta().setNote(socketHandler.getSocketId().toString());
            message.getContent().setFeature(MessageFeature.RELIABLE);
            socketHandler.setUserId(userId);
            socketHandler.setAuthStatus(AuthStatus.AUTHENTICATING);
            networkUserService.addPendingUser(userId, socketHandler);
            this.postOffice.deliveryMessage(message);
        }
    }
}
