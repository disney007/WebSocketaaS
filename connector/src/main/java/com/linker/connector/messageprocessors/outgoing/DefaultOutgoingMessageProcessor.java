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
    public void doProcess(Message message, Object data, MessageContext context) {
        Address targetAddress = message.getMeta().getTargetAddress();
        SocketHandler handler = networkUserService.getUser(message.getTo(), targetAddress.getSocketId());

        if (handler != null) {
            ChannelFuture channelFuture = handler.sendMessage(message);
            channelFuture.addListener(future -> {
                if (message.getMeta().isConfirmEnabled()
                        || (!future.isSuccess() && isReliableMessage(message))) {
                    MessageState state = future.isSuccess() ? MessageState.PROCESSED : MessageState.NETWORK_ERROR;
                    postOffice.deliverStateChangedMessage(message, state);
                }
            });


        } else {
            log.warn("user [{}] not found on [{}]", message.getTo(), targetAddress);
            if (isReliableMessage(message)) {
                postOffice.deliverStateChangedMessage(message, MessageState.ADDRESS_NOT_FOUND);
            }
        }

    }

    boolean isReliableMessage(Message message) {
        return message.getContent().getFeature() == MessageFeature.RELIABLE;
    }
}
