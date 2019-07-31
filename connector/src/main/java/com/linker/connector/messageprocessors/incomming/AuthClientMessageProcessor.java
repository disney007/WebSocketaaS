package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageType;
import com.linker.common.messages.AuthClient;
import com.linker.connector.AuthStatus;
import com.linker.connector.NetworkUserService;
import com.linker.connector.express.PostOffice;
import com.linker.connector.network.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthClientMessageProcessor extends IncomingMessageProcessor<AuthClient> {

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
    public void doProcess(Message message, AuthClient data, SocketHandler socketHandler) {
        if (socketHandler.getAuthStatus() == AuthStatus.NOT_AUTHENTICATED) {
            String userId = data.getUserId();
            message.setFrom(userId);
            message.getMeta().setNote(socketHandler.getSocketId().toString());
            message.getContent().setFeature(MessageFeature.RELIABLE);
            socketHandler.setUserId(userId);
            socketHandler.setAuthStatus(AuthStatus.AUTHENTICATING);
            networkUserService.addPendingUser(userId, socketHandler);
            this.postOffice.deliverMessage(message);
        }
    }
}
