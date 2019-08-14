package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.UserDisconnected;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.express.PostOffice;
import com.linker.processor.services.UserChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDisconnectedMessageProcessor extends MessageProcessor<UserDisconnected> {

    final UserChannelService userChannelService;
    final ProcessorUtils processorUtils;
    final PostOffice postOffice;


    @Override
    public MessageType getMessageType() {
        return MessageType.USER_DISCONNECTED;
    }

    @Override
    public void doProcess(Message message, UserDisconnected data, MessageContext context) {
        if (!processorUtils.isProcessorMessage(message)) {
            postOffice.deliverMessage(message);
            return;
        }

        if (message.getState() == MessageState.CREATED) {
            log.info("user [{}] disconnected", data.getUserId());
            userChannelService.removeAddress(data.getUserId(), message.getMeta().getOriginalAddress());
        }

        log.info("send user [{}] disconnected message to master user", data.getUserId());
        processorUtils.sendMessageToMasterUser(message, data.getUserId());
    }
}
