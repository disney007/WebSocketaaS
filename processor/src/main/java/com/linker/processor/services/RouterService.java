package com.linker.processor.services;

import com.google.common.collect.ImmutableList;
import com.linker.common.Message;
import com.linker.common.MessageContentOutput;
import com.linker.common.MessageMeta;
import com.linker.common.MessageUtils;
import com.linker.common.exceptions.RouterNotConnectedException;
import com.linker.common.router.Router;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.messageprocessors.MessageProcessorService;
import com.linker.processor.models.RouterTunnel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouterService {

    final ApplicationConfig applicationConfig;

    final MetaServerService metaServerService;

    final ProcessorUtils processorUtils;

    final MessageProcessorService messageProcessorService;

    final RouterTunnelService routerTunnelService;

    final RouterGraphService routerGraphService;


    @PostConstruct
    public void init() throws IOException {
        if (processorUtils.isDomainRouter()) {
            setupRouterToRouterConnections();
        } else {
            setupDomainToRouterConnection();
        }
    }

    void setupDomainToRouterConnection() throws IOException {
        log.info("set up domain to router connection");
        final String domainName = applicationConfig.getDomainName();
        List<Router> routers = metaServerService.getRouters();
        Optional<Router> routerOptional = routers.stream().filter(router -> router.getDomains().contains(domainName)).findAny();
        if (routerOptional.isPresent()) {
            Router router = routerOptional.get();
            this.routerTunnelService.loadRouters(ImmutableList.of(router))
                    .getDefaultTunnel().onMessage(this::onMessage);
        } else {
            log.info("domain [{}] is not connected to any router", domainName);
        }
    }

    void setupRouterToRouterConnections() throws IOException {
        log.info("set up router to router connections");
        routerGraphService.loadGraph(metaServerService.getRouterGraph());
        List<Router> linkedRouters = routerGraphService.getCurrentLinkedRouters();
        log.info("link routers {}", linkedRouters);
        routerTunnelService.loadRouters(linkedRouters)
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
        log.info("message received from router: [{}]", msgWrapper);
        this.messageProcessorService.process(msgWrapper);
    }

    void onMessage(MessageContentOutput content) {
        switch (content.getType()) {
            case INTERNAL_MESSAGE:
                handleInternalMessage(content);
                return;
            default:
                log.info("unhandled message received from router: [{}]", content.getType());
        }
    }

    public Router getCurrentRouter() {
        return this.routerGraphService.getCurrentRouter();
    }

    public String getRouterName() {
        return getCurrentRouter().getName();
    }

    public boolean isDomainLinkedToCurrentRouter(String domainName) {
        Router router = getCurrentRouter();
        return router.getDomains().contains(domainName);
    }

    public String getNextRouterName(String targetDomainName) {
        Router targetRouter = routerGraphService.getDomainLinkedRouter(targetDomainName);
        if (targetRouter == null) {
            return null;
        }
        return routerGraphService.getNextRouterName(targetRouter.getName());
    }

    public void sendMessage(Message message) throws RouterNotConnectedException {
        RouterTunnel tunnel = routerTunnelService.getDefaultTunnel();
        log.info("send message from domain [{}] to router [{}]", applicationConfig.getDomainName(), tunnel.getName());
        tunnel.sendMessage(message);
    }
}
