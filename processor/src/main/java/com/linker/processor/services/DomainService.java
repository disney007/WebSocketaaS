package com.linker.processor.services;

import com.linker.common.Message;
import com.linker.common.MessageContentOutput;
import com.linker.common.MessageMeta;
import com.linker.common.MessageUtils;
import com.linker.common.router.Domain;
import com.linker.processor.express.PostOffice;
import com.linker.processor.messageprocessors.MessageProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainService {

    final MetaServerService metaServerService;

    final PostOffice postOffice;

    final DomainTunnelService domainTunnelService;

    final DomainGraphService domainGraphService;


    @PostConstruct
    public void init() throws IOException {
        log.info("set up domain connections");
        domainGraphService.loadGraph(metaServerService.getDomainGraph());
        List<Domain> linkedDomains = domainGraphService.getCurrentLinkedDomains();
        log.info("link domains {}", linkedDomains);
        domainTunnelService.loadDomains(linkedDomains)
                .getTunnels().forEach(routerTunnel -> routerTunnel.onMessage(this::onMessage));
    }

    void handleInternalMessage(MessageContentOutput content) {
        Message internalMessage = content.getData(Message.class);
        Message msgWrapper = Message.builder()
                .from(internalMessage.getFrom())
                .to(internalMessage.getTo())
                .meta(new MessageMeta(internalMessage.getMeta().getOriginalAddress()))
                .content(MessageUtils.createMessageContent(content.getType(), content.getData(), internalMessage.getContent().getFeature()))
                .build();
        this.postOffice.onMessageArrived(msgWrapper, "Tunnel");
    }

    void onMessage(MessageContentOutput content) {
        switch (content.getType()) {
            case INTERNAL_MESSAGE:
                handleInternalMessage(content);
                return;
            default:
                log.info("unhandled message received from domain: [{}]", content.getType());
        }
    }

    public Domain getCurrentDomain() {
        return this.domainGraphService.getCurrentDomain();
    }

    public String getDomainName() {
        return getCurrentDomain().getName();
    }


    public String getNextDomainName(String targetDomainName) {
        return domainGraphService.getNextDomainName(targetDomainName);
    }
}
