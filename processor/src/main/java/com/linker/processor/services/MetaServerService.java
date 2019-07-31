package com.linker.processor.services;

import com.google.common.collect.ImmutableList;
import com.linker.common.client.ClientApp;
import com.linker.common.router.Router;
import com.linker.common.router.RouterGraph;
import com.linker.common.router.RouterLink;
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
    static class ClientAppList extends ArrayList<ClientApp> {
    }

    static class RouterList extends ArrayList<Router> {
    }

    static class RouterLinkList extends ArrayList<RouterLink> {
    }

    final ApplicationConfig applicationConfig;
    final HttpService httpService;


    boolean isMetaServerEnabled() {
        return StringUtils.isNotBlank(applicationConfig.getMetaServerUrl());
    }

    public String buildUrl(String path) {
        return Paths.get(applicationConfig.getMetaServerUrl(), path).toString();
    }

    List<ClientApp> getClientApps() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/clientApps"), null, ClientAppList.class);
        } else {
            return ImmutableList.of();
        }
    }

    List<Router> getRouters() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/routers"), null, RouterList.class);
        } else {
            return ImmutableList.of();
        }
    }

    List<RouterLink> getRouterLinks() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/routers/links"), null, RouterLinkList.class);
        } else {
            return ImmutableList.of();
        }
    }

    RouterGraph getRouterGraph() throws IOException {
        if (isMetaServerEnabled()) {
            return httpService.getSync(buildUrl("/routers/graph"), null, RouterGraph.class);
        } else {
            return null;
        }
    }
}
