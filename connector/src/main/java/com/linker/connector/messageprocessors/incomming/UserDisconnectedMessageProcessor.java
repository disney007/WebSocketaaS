package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.models.UserDisconnectedMessage;
import com.linker.connector.MessageService;
import com.linker.connector.NetworkUserService;
import com.linker.connector.SocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserDisconnectedMessageProcessor extends IncomingMessageProcessor<UserDisconnectedMessage> {

    @Autowired
    NetworkUserService networkUserService;

    @Autowired
    MessageService messageService;

    @Override
    public void doProcess(Message message, UserDisconnectedMessage data, SocketHandler SocketHandler) throws IOException {
        String userId = data.getUserId();
        if (StringUtils.isNotEmpty(userId)) {
            networkUserService.removeUser(userId);
            networkUserService.removePendingUser(userId);
            messageService.sendMessage(message);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_DISCONNECTED;
    }
}
