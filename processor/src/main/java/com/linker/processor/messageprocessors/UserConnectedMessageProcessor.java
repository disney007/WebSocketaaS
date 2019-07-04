package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.messages.UserConnected;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UserConnectedMessageProcessor extends MessageProcessor<UserConnected> {
    @Autowired
    UserChannelService userChannelService;

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_CONNECTED;
    }

    @Override
    public void doProcess(Message message, UserConnected data, MessageContext context) throws IOException {
        log.info("user [{}] connected", data.getUserId());
        userChannelService.addAddress(data.getUserId(), message.getMeta().getOriginalAddress());
    }
}
