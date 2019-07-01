package com.linker.connector.messageprocessors.incomming;

import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.EmptyMessage;
import com.linker.connector.AuthStatus;
import com.linker.connector.PostOffice;
import com.linker.connector.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DefaultIncomingMessageProcessor extends IncomingMessageProcessor<Object> {

    @Autowired
    PostOffice postOffice;

    public DefaultIncomingMessageProcessor() {

    }

    @Override
    public void doProcess(Message message, Object data, SocketHandler socketHandler) throws IOException {
        if (socketHandler.getAuthStatus() == AuthStatus.AUTHENTICATED) {
            this.postOffice.deliveryMessage(message);
        } else {
            message = new Message.MessageBuilder()
                    .from(Keywords.SYSTEM)
                    .content(MessageUtils.createMessageContent(MessageType.AUTH_REQUIRED, new EmptyMessage(), MessageFeature.RELIABLE))
                    .build();
            socketHandler.sendMessage(message);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ANY;
    }
}
