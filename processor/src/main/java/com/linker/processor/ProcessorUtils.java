package com.linker.processor;

import com.linker.common.Address;
import com.linker.common.Message;
import com.linker.common.MessageMeta;
import com.linker.common.MessageState;
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

    public <T> boolean sendMessageToMasterUser(Message message, String refUserId) throws IOException {
        ClientApp clientApp = clientAppService.getClientAppByUserId(refUserId);
        if (clientApp != null) {
            String masterUserId = clientApp.getMasterUserId();

            if (!refUserId.equals(masterUserId) && StringUtils.isNotEmpty(masterUserId)) {
                message.setTo(masterUserId);
                try {
                    postOffice.deliveryMessage(message);
                } catch (AddressNotFoundException e) {
                    log.info("address not found for user [{}]", message.getTo());
                    message.setState(MessageState.TARGET_NOT_FOUND);
                    messageRepository.save(message);
                }
                return true;
            }
        }
        return false;
    }

    public MessageMeta getOriginalAddressMeta() {
        return new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName()));
    }
}
