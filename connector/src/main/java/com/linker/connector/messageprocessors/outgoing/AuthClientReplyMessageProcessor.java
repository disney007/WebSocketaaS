package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.ResultStatus;
import com.linker.common.models.AuthClientReplyMessage;
import com.linker.common.models.UserConnectedMessage;
import com.linker.connector.PostOffice;
import com.linker.connector.NetworkUserService;
import com.linker.connector.SocketHandler;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AuthClientReplyMessageProcessor extends OutgoingMessageProcessor<AuthClientReplyMessage> {
    @Autowired
    NetworkUserService networkUserService;

    @Autowired
    PostOffice messageService;

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT_REPLY;
    }

    @Override
    public void doProcess(Message message, AuthClientReplyMessage data, MessageContext context) throws IOException {
        String userId = data.getUserId();
        SocketHandler socketHandler = networkUserService.removePendingUser(userId);
        if (data.getResult().getStatus() == ResultStatus.OK) {
            log.info("user [{}] is authenticated", userId);
            networkUserService.addUser(userId, socketHandler);
            socketHandler.sendMessage(message);

            Message userConnectedMessage = Message.builder()
                    .content(
                            MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnectedMessage(userId),
                                    message.getContent().getFeature())
                    )
                    .from(Keywords.SYSTEM)
                    .build();
            messageService.deliveryMessage(userConnectedMessage);
        } else {
            socketHandler.sendMessage(message).addListener((ChannelFutureListener) future -> socketHandler.close());
        }
    }
}
