package com.linker.connector.messageprocessors.outgoing;

import com.linker.common.*;
import com.linker.common.messages.MessageStateChanged;
import com.linker.connector.NetworkUserService;
import com.linker.connector.configurations.ApplicationConfig;
import com.linker.connector.express.PostOffice;
import com.linker.connector.network.SocketHandler;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DefaultOutgoingMessageProcessor extends OutgoingMessageProcessor<Object> {
    @Autowired
    NetworkUserService networkUserService;
    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.ANY;
    }

    @Override
    public void doProcess(Message message, Object data, MessageContext context) throws IOException {
        Address targetAddress = message.getMeta().getTargetAddress();
        SocketHandler handler = networkUserService.getUser(message.getTo(), targetAddress.getSocketId());

        if (handler != null) {
            ChannelFuture channelFuture = handler.sendMessage(message);
            if (message.getMeta().isConfirmEnabled()) {
                channelFuture.addListener(future -> {
                    MessageState state = future.isSuccess() ? MessageState.PROCESSED : MessageState.NETWORK_ERROR;
                    confirmMessage(message, state);
                });
            }

        } else {
            log.warn("user [{}] not found on [{}]", message.getTo(), targetAddress);
            confirmMessage(message, MessageState.TARGET_NOT_FOUND);
        }

    }

    void confirmMessage(Message message, MessageState state) throws IOException {
        Message confirmMessage = Message.builder()
                .from(Keywords.SYSTEM)
                .meta(new MessageMeta(message.getMeta().getTargetAddress()))
                .content(
                        MessageUtils.createMessageContent(MessageType.MESSAGE_STATE_CHANGED,
                                new MessageStateChanged(message.toContentlessMessage(), state), MessageFeature.RELIABLE)
                )
                .build();
        postOffice.deliveryMessage(confirmMessage);
    }
}
