package com.linker.connector.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageType;
import com.linker.common.models.AuthClientMessage;
import com.linker.connector.MessageService;
import com.linker.connector.WebSocketHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class AuthClientMessageProcessor extends IncomingMessageProcessor<AuthClientMessage> {

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT;
    }

    public AuthClientMessageProcessor(MessageService messageService) {
        super(messageService);
    }

    @Override
    public void doProcess(Message message, AuthClientMessage data, WebSocketHandler socketHandler) throws IOException {
        this.messageService.sendMessage(message);
    }
}
