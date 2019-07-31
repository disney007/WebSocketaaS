package com.linker.processor;

import com.linker.common.*;
import com.linker.common.client.ClientApp;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.NameRecord;
import com.linker.processor.repositories.MessageRepository;
import com.linker.processor.services.ClientAppService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    MessageRepository messageRepository;

    @Autowired
    MessageProcessorService messageProcessorService;

    @Getter
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public <T> boolean sendMessageToMasterUser(Message message, String refUserId) {
        ClientApp clientApp = clientAppService.getClientAppByUserId(refUserId);
        if (clientApp != null) {
            String masterUserId = clientApp.getMasterUserId();

            if (!refUserId.equals(masterUserId) && StringUtils.isNotEmpty(masterUserId)) {
                message.setTo(masterUserId);
                messageProcessorService.process(message);
                return true;
            }
        }
        return false;
    }

    public MessageMeta getOriginalAddressMeta() {
        return new MessageMeta(new Address(applicationConfig.getDomainName(), applicationConfig.getProcessorName()));
    }

    public boolean isCurrentDomainMessage(Message message) {
        final String currentDomainName = applicationConfig.getDomainName();

        if (message.getMeta().getTargetAddress() != null) {
            return StringUtils.equalsIgnoreCase(currentDomainName, message.getMeta().getTargetAddress().getDomainName());
        }

        if (StringUtils.isBlank(message.getTo())) {
            return true;
        }

        String targetDomainName = clientAppService.resolveDomain(message.getTo());
        return StringUtils.equalsIgnoreCase(currentDomainName, targetDomainName);
    }

    public boolean isProcessorMessage(Message message) {
        String to = message.getTo();
        return StringUtils.isNotBlank(to) && StringUtils.startsWithIgnoreCase(to, Keywords.PROCESSOR);
    }

    public boolean isConnectorMessage(Message message) {
        String to = message.getTo();
        return StringUtils.isNotBlank(to) && StringUtils.startsWithIgnoreCase(to, Keywords.CONNECTOR);
    }

    /**
     * domain-01 => domain01-1
     */
    public String resolveDomainUserId(String domainName) {
        NameRecord nameRecord = NameRecord.parse(domainName);
        return nameRecord.getAppName() + nameRecord.getNumber() + "-1";
    }

    public String resolveDomainAppName(String domainName) {
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
