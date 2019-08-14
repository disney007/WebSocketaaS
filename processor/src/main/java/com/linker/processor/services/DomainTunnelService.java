package com.linker.processor.services;

import com.linker.common.router.Domain;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.models.DomainTunnel;
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
public class DomainTunnelService {
    Map<String, DomainTunnel> tunnelMap = new HashMap<>();

    final MetaServerService metaServerService;
    final ApplicationContext applicationContext;
    final ProcessorUtils processorUtils;

    @Autowired
    public DomainTunnelService(MetaServerService metaServerService, ApplicationContext applicationContext, ProcessorUtils processorUtils) throws IOException {
        this.metaServerService = metaServerService;
        this.applicationContext = applicationContext;
        this.processorUtils = processorUtils;
    }

    DomainTunnel createTunnel(Domain domain) {
        DomainTunnel domainTunnel = new DomainTunnel();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(domainTunnel);
        domainTunnel.connectDomain(domain);
        return domainTunnel;
    }

    DomainTunnelService loadDomains(List<Domain> domains) throws IOException {
        log.info("loading domains {}", domains);
        tunnelMap = domains
                .stream()
                .map(this::createTunnel)
                .collect(Collectors.toMap(DomainTunnel::getName, t -> t));
        return this;
    }

    public Collection<DomainTunnel> getTunnels() {
        return this.tunnelMap.values();
    }
}
