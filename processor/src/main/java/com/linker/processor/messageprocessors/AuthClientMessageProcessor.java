package com.linker.processor.messageprocessors;

import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageResult;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.ResultStatus;
import com.linker.common.models.AuthClientMessage;
import com.linker.common.models.AuthClientReplyMessage;
import com.linker.processor.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class AuthClientMessageProcessor extends MessageProcessor<AuthClientMessage> {
    @Autowired
    MessageService messageService;

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_CLIENT;
    }

    @Override
    public void doProcess(Message message, AuthClientMessage data, MessageContext context) throws IOException {
        MessageResult result = MessageResult.ok();
        if ("ANZ-wrong".equals(data.getUserId())) {
            result = new MessageResult(ResultStatus.FAILED, "wrong user name or token");
        }
        AuthClientReplyMessage replyMessageData = new AuthClientReplyMessage(result);
        replyMessageData.setAppId(data.getAppId());
        replyMessageData.setToken(data.getToken());
        replyMessageData.setUserId(data.getUserId());
        MessageContent content = MessageUtils.createMessageContent(MessageType.AUTH_CLIENT_REPLY, replyMessageData);

        Message replyMessage = Message.builder()
                .content(content)
                .from(Keywords.SYSTEM)
                .to(message.getFrom())
                .meta(message.getMeta())
                .build();
        messageService.sendMessage(replyMessage);
    }
}
