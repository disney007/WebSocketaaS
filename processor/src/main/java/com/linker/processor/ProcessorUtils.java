package com.linker.processor;

import com.linker.common.Address;
import com.linker.common.Keywords;
import com.linker.common.Message;
import com.linker.common.MessageFeature;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.models.ClientApp;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.ClientAppService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ProcessorUtils {

    @Autowired
    ClientAppService clientAppService;
    @Autowired
    ApplicationConfig applicationConfig;
    @Autowired
    PostOffice postOffice;
    @Autowired
    MessageRepository messageRepository;

    public <T> void sendMessageToMasterUser(MessageType messageType, T data, String refUserId) throws IOException {
        ClientApp clientApp = clientAppService.getClientAppByUserId(refUserId);
        if (clientApp != null) {
            String masterUserId = clientApp.getMasterUserId();

            if (!refUserId.equals(masterUserId) && StringUtils.isNotEmpty(masterUserId)) {
                Message message = Message.builder()
                        .from(Keywords.SYSTEM)
                        .to(masterUserId)
                        .meta(new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName())))
                        .content(MessageUtils.createMessageContent(messageType, data, MessageFeature.RELIABLE))
                        .build();
                try {
                    postOffice.deliveryMessage(message);
                } catch (AddressNotFoundException e) {
                    log.info("address not found for user [{}]", message.getTo());
                    message.setState(MessageState.TARGET_NOT_FOUND);
                    messageRepository.save(message);
                }
            }
        }
    }
}
