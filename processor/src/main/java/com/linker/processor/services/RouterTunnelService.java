package com.linker.processor.services;

import com.linker.common.router.Router;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.models.RouterTunnel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouterTunnelService {
    Map<String, RouterTunnel> tunnelMap = new HashMap<>();

    final MetaServerService metaServerService;
    final ApplicationContext applicationContext;
    final ProcessorUtils processorUtils;

    @Autowired
    public RouterTunnelService(MetaServerService metaServerService, ApplicationContext applicationContext, ProcessorUtils processorUtils) throws IOException {
        this.metaServerService = metaServerService;
        this.applicationContext = applicationContext;
        this.processorUtils = processorUtils;
    }

    RouterTunnel createTunnel(Router router) {
        RouterTunnel routerTunnel = new RouterTunnel();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(routerTunnel);
        routerTunnel.connectRouter(router);
        return routerTunnel;
    }

    RouterTunnelService loadRouters(List<Router> routers) throws IOException {
        log.info("loading routers {}", routers);
        tunnelMap = routers
                .stream()
                .map(this::createTunnel)
                .collect(Collectors.toMap(RouterTunnel::getName, t -> t));
        return this;
        //TODO: diff the existing and new routers
    }

    public RouterTunnel getDefaultTunnel() {
        if (!this.processorUtils.isDomainProcessor()) {
            throw new IllegalStateException("expect current domain to be domain processor");
        }

        if (this.tunnelMap.size() != 1) {
            throw new IllegalStateException("expect tunnel map size to be 1, but " + this.tunnelMap.size());
        }

        return this.tunnelMap.entrySet().iterator().next().getValue();
    }

    public Collection<RouterTunnel> getTunnels() {
        return this.tunnelMap.values();
    }
}
