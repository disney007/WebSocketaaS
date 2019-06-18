package com.linker.connector.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.Utils;
import com.linker.common.models.AuthClientMessage;
import com.linker.connector.MessageService;
import com.linker.connector.NetworkUserService;
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

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT;
    }

    public AuthClientMessageProcessor(MessageService messageService) {
        super(messageService);
    }

    @Override
    public void doProcess(Message message, AuthClientMessage data, SocketHandler socketHandler) throws IOException {
        String userId = Utils.normaliseUserId(data.getUserId());
        message.setFrom(userId);
        socketHandler.setUserId(userId);
        networkUserService.addUser(userId, socketHandler);
        this.messageService.sendMessage(message);
    }
}
