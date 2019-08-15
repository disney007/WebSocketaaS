package com.linker.processor.services;

import com.linker.common.*;
import com.linker.common.exceptions.NotConnectedException;
import com.linker.common.messages.FetchMissingMessagesComplete;
import com.linker.common.messages.FetchMissingMessagesRequest;
import com.linker.common.router.Domain;
import com.linker.processor.express.PostOffice;
import com.linker.processor.models.DomainTunnel;
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
        domainTunnelService.loadDomains(linkedDomains).getTunnels().forEach(routerTunnel -> {
            routerTunnel.onMessage(this::onMessage);
            routerTunnel.onAuthenticated(this::onAuthenticated);
        });
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

    void fetchMissingMessages(DomainTunnel tunnel) {
        log.info("fetch missing messages from domain [{}]", tunnel.getDomain().getName());
        MessageContent msg = MessageUtils.createMessageContent(MessageType.FETCH_MISSING_MESSAGES_REQUEST,
                new FetchMissingMessagesRequest(2000), MessageFeature.RELIABLE);
        try {
            tunnel.sendMessage(msg);
        } catch (NotConnectedException e) {
            log.warn("domain is not connected, ignore");
        }
    }

    void handleFetchingMessagesComplete(DomainTunnel tunnel, MessageContentOutput content) {
        FetchMissingMessagesComplete data = content.getData(FetchMissingMessagesComplete.class);
        if (data.getLeftMissingCount() > 0) {
            log.info("{} missing messages left in domain [{}], continue fetching", data.getLeftMissingCount(), tunnel.getDomain().getName());
            fetchMissingMessages(tunnel);
            return;
        }
        log.info("fetching missing messages complete from domain [{}]", tunnel.getDomain().getName());
    }

    void onAuthenticated(DomainTunnel tunnel) {
        this.fetchMissingMessages(tunnel);
    }

    void onMessage(DomainTunnel tunnel, MessageContentOutput content) {
        switch (content.getType()) {
            case INTERNAL_MESSAGE:
                handleInternalMessage(content);
                return;
            case FETCH_MISSING_MESSAGES_COMPLETE:
                handleFetchingMessagesComplete(tunnel, content);
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
