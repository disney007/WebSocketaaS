package com.linker.processor;

import com.linker.common.*;
import com.linker.common.client.ClientApp;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.express.PostOffice;
import com.linker.processor.models.NameRecord;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.ClientAppService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    @Getter
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public <T> boolean sendMessageToMasterUser(Message message, String refUserId) throws IOException {
        ClientApp clientApp = clientAppService.getClientAppByUserId(refUserId);
        if (clientApp != null) {
            String masterUserId = clientApp.getMasterUserId();

            if (!refUserId.equals(masterUserId) && StringUtils.isNotEmpty(masterUserId)) {
                message.setTo(masterUserId);
                try {
                    postOffice.deliveryMessage(message);
                } catch (AddressNotFoundException e) {
                    log.info("address not found for user [{}]", message.getTo(), e);
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

    public boolean isDomainProcessor() {
        return StringUtils.startsWithIgnoreCase(applicationConfig.getDomainName(), "domain");
    }

    public boolean isDomainRouter() {
        return StringUtils.startsWithIgnoreCase(applicationConfig.getDomainName(), "router");
    }

    /**
     * domain-01 => domain01-1
     */
    public String resolveRouterUserId(String domainName) {
        NameRecord nameRecord = NameRecord.parse(domainName);
        return nameRecord.getAppName() + nameRecord.getNumber() + "-1";
    }

    public String resolveRouterAppName(String domainName) {
        NameRecord nameRecord = NameRecord.parse(domainName);
        return nameRecord.getAppName() + nameRecord.getNumber();
    }

    public void assertInternalMessage(Message message) {
        if (message.getContent().getType() != MessageType.INTERNAL_MESSAGE) {
            StringBuilder sb = new StringBuilder();
            sb.append("message must be INTERNAL_MESSAGE, but is ").append(message.getContent().getType());
            sb.append("\n");
            sb.append(message.toString());
            throw new IllegalStateException(sb.toString());
        }
    }
}
