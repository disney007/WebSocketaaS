package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.client.ClientApp;
import com.linker.common.messages.CloseConnection;
import com.linker.common.messages.EmptyMessage;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.express.PostOffice;
import com.linker.processor.services.ClientAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloseConnectionMessageProcessor extends MessageProcessor<CloseConnection> {
    final PostOffice postOffice;
    final ApplicationConfig applicationConfig;
    final ClientAppService clientAppService;

    @Override
    public MessageType getMessageType() {
        return MessageType.CLOSE_CONNECTION;
    }

    @Override
    public void doPreprocess(Message message, CloseConnection data, MessageContext context) {
        message.setTo(data.getUserId());
    }

    @Override
    public void doProcess(Message message, CloseConnection data, MessageContext context) {
        ClientApp targetClientApp = clientAppService.getClientAppByUserId(data.getUserId());
        if (targetClientApp != null && StringUtils.equals(targetClientApp.getMasterUserId(), message.getFrom())) {
            message.getContent().setData(new EmptyMessage());
            message.getContent().setFeature(MessageFeature.RELIABLE);
            postOffice.deliverMessage(message);
        } else {
            log.info("can not close connection because [{}] is not the master id of [{}]", message.getFrom(), data.getUserId());
        }
    }
}
