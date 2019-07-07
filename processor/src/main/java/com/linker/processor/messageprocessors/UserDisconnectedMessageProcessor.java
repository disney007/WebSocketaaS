package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContext;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.messages.UserDisconnected;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.UserChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UserDisconnectedMessageProcessor extends PersistableMessageProcessor<UserDisconnected> {

    UserChannelService userChannelService;
    ProcessorUtils processorUtils;

    @Autowired
    public UserDisconnectedMessageProcessor(MessageRepository messageRepository, UserChannelService userChannelService, ProcessorUtils processorUtils) {
        super(messageRepository);
        this.userChannelService = userChannelService;
        this.processorUtils = processorUtils;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_DISCONNECTED;
    }

    @Override
    public void doProcess(Message message, UserDisconnected data, MessageContext context) throws IOException {
        log.info("user [{}] disconnected", data.getUserId());
        userChannelService.removeAddress(data.getUserId(), message.getMeta().getOriginalAddress());

        log.info("send user [{}] disconnected message to master user", data.getUserId());
        boolean result = processorUtils.sendMessageToMasterUser(message, data.getUserId());
        if (!result) {
            updateMessageState(message, MessageState.PROCESSED);
        }
    }
}
