package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageMeta;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.ResultStatus;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.messages.UserConnected;
import com.linker.connector.AuthStatus;
import com.linker.connector.NetworkUserService;
import com.linker.connector.express.PostOffice;
import com.linker.connector.SocketHandler;
import com.linker.connector.configurations.ApplicationConfig;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class AuthClientReplyMessageProcessor extends OutgoingMessageProcessor<AuthClientReply> {
    @Autowired
    NetworkUserService networkUserService;

    @Autowired
    PostOffice postOffice;

    @Autowired
    ApplicationConfig applicationConfig;

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT_REPLY;
    }

    @Override
    public void doProcess(Message message, AuthClientReply data, MessageContext context) throws IOException {
        String userId = data.getUserId();
        Long socketId = Long.parseLong(message.getMeta().getNote());
        SocketHandler socketHandler = networkUserService.getPendingUser(userId, socketId);
        networkUserService.removePendingUser(userId, socketId);

        if (data.getResult().getStatus() == ResultStatus.OK) {
            log.info("user [{}] is authenticated", userId);
            networkUserService.addUser(userId, socketHandler);
            socketHandler.setAuthStatus(AuthStatus.AUTHENTICATED);
            socketHandler.sendMessage(message);

            MessageMeta meta = new MessageMeta();
            meta.setOriginalAddress(new Address(applicationConfig.getDomainName(), applicationConfig.getConnectorName(), socketId));
            Message userConnectedMessage = Message.builder()
                    .content(
                            MessageUtils.createMessageContent(MessageType.USER_CONNECTED, new UserConnected(userId),
                                    message.getContent().getFeature())
                    )
                    .from(Keywords.SYSTEM)
                    .meta(meta)
                    .build();
            postOffice.deliveryMessage(userConnectedMessage);
        } else {
            socketHandler.setAuthStatus(AuthStatus.NOT_AUTHENTICATED);
            socketHandler.sendMessage(message).addListener((ChannelFutureListener) future -> socketHandler.close());
        }
    }
}
