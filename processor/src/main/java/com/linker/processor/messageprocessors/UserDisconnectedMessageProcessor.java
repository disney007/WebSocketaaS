package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.messages.UserDisconnected;
import com.linker.processor.repositories.UserChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UserDisconnectedMessageProcessor extends MessageProcessor<UserDisconnected> {
    @Autowired
    UserChannelRepository userChannelRepository;

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_DISCONNECTED;
    }

    @Override
    public void doProcess(Message message, UserDisconnected data, MessageContext context) throws IOException {
        log.info("user [{}] disconnected", data.getUserId());
        userChannelRepository.findById(data.getUserId())
                .ifPresent(userChannel -> {
                    userChannel.getAddresses().remove(message.getMeta().getOriginalAddress());
                    if (userChannel.getAddresses().size() == 0) {
                        userChannelRepository.delete(userChannel);
                    } else {
                        userChannelRepository.save(userChannel);
                    }
                });
    }
}
