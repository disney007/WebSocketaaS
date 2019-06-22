package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.models.UserConnectedMessage;
import com.linker.processor.models.UserChannel;
import com.linker.processor.repositories.UserChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@Service
@Slf4j
public class UserConnectedMessageProcessor extends MessageProcessor<UserConnectedMessage> {
    @Autowired
    UserChannelRepository userChannelRepository;

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_CONNECTED;
    }

    @Override
    public void doProcess(Message message, UserConnectedMessage data, MessageContext context) throws IOException {
        log.info("user [{}] connected", data.getUserId());
        UserChannel userChannel = userChannelRepository.findById(data.getUserId()).orElse(null);
        if (userChannel == null) {
            userChannel = new UserChannel(data.getUserId(), new ArrayList<>(), ZonedDateTime.now().toInstant().toEpochMilli());
        }
        userChannel.getAddresses().add(message.getMeta().getOriginalAddress());
        userChannelRepository.save(userChannel);
    }
}
