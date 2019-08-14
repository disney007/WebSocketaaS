package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.messages.UserConnected;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.express.PostOffice;
import com.linker.processor.services.UserChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserConnectedMessageProcessor extends MessageProcessor<UserConnected> {

    final UserChannelService userChannelService;
    final ProcessorUtils processorUtils;
    final PostOffice postOffice;

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_CONNECTED;
    }

    @Override
    public void doProcess(Message message, UserConnected data, MessageContext context) {
        if (!processorUtils.isProcessorMessage(message)) {
            postOffice.deliverMessage(message);
            return;
        }

        if (message.getState() == MessageState.CREATED) {
            log.info("user [{}] connected", data.getUserId());
            userChannelService.addAddress(data.getUserId(), message.getMeta().getOriginalAddress());
        }

        log.info("send user [{}] connected message to master user", data.getUserId());
        processorUtils.sendMessageToMasterUser(message, data.getUserId());
    }
}
