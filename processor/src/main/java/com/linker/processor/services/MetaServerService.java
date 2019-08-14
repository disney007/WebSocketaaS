package com.linker.processor.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.client.ClientApp;
import com.linker.common.router.Domain;
import com.linker.common.router.DomainGraph;
import com.linker.common.router.DomainLink;
import com.linker.processor.configurations.ApplicationConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaServerService {
    private static class ClientAppList extends ArrayList<ClientApp> {
    }

    private static class DomainList extends ArrayList<Domain> {
    }

    private static class DomainLinkList extends ArrayList<DomainLink> {
    }

    final ApplicationConfig applicationConfig;
    final HttpService httpService;


    boolean isMetaServerEnabled() {
        return StringUtils.isNotBlank(applicationConfig.getMetaServerUrl());
    }

    public String buildUrl(String path) {
        return Paths.get(applicationConfig.getMetaServerUrl(), path).toString();
    }

    public List<ClientApp> getClientApps() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/clientApps"), null, ClientAppList.class);
        } else {
            return ImmutableList.of();
        }
    }

    public List<Domain> getDomains() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/domains"), null, DomainList.class);
        } else {
            return ImmutableList.of();
        }
    }

    public List<DomainLink> getDomainLinks() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/domains/links"), null, DomainLinkList.class);
        } else {
            return ImmutableList.of();
        }
    }

    public DomainGraph getDomainGraph() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/domains/graph"), null, DomainGraph.class);
        } else {
            DomainGraph graph = new DomainGraph();
            graph.setLinks(ImmutableSet.of());
            graph.setDomains(ImmutableList.of());
            return graph;
        }
    }
}
