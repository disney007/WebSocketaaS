package com.linker.processor.messageprocessors;

import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageResult;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.messages.AuthClient;
import com.linker.common.messages.AuthClientReply;
import com.linker.processor.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class AuthClientMessageProcessor extends MessageProcessor<AuthClient> {
    @Autowired
    PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT;
    }

    @Override
    public void doProcess(Message message, AuthClient data, MessageContext context) throws IOException {
        MessageResult result = MessageResult.ok();
        AuthClientReply replyMessageData = new AuthClientReply(result);
        replyMessageData.setAppId(data.getAppId());
        replyMessageData.setToken(data.getToken());
        replyMessageData.setUserId(data.getUserId());
        MessageContent content = MessageUtils.createMessageContent(MessageType.AUTH_CLIENT_REPLY, replyMessageData,
                message.getContent().getFeature());

        Message replyMessage = Message.builder()
                .content(content)
                .from(Keywords.SYSTEM)
                .to(message.getMeta().getOriginalAddress().getConnectorName())
                .meta(message.getMeta())
                .build();
        postOffice.deliveryMessage(replyMessage);
    }
}
