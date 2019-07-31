package com.linker.processor.services;

import com.linker.common.client.UserDistribution;
import com.linker.processor.configurations.ApplicationConfig;
import com.linker.common.client.ClientApp;
import com.linker.processor.models.NameRecord;
import com.linker.processor.repositories.ClientAppRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.linker.common.Utils.defaultValue;

@Slf4j
@Service
public class ClientAppService {

    final ApplicationConfig applicationConfig;

    final ClientAppRepository clientAppRepository;

    final MetaServerService metaServerService;

    ConcurrentHashMap<String, ClientApp> cache = new ConcurrentHashMap<>();

    @Autowired
    public ClientAppService(ApplicationConfig applicationConfig, ClientAppRepository clientAppRepository, MetaServerService metaServerService) {
        this.applicationConfig = applicationConfig;
        this.clientAppRepository = clientAppRepository;
        this.metaServerService = metaServerService;
    }

    @PostConstruct
    public void init() throws IOException {
        cache.clear();

        log.info("load client apps from application config");
        applicationConfig.getClientApps().forEach(clientAppRepository::save);

        log.info("load client apps from meta server");
        metaServerService.getClientApps().forEach(clientAppRepository::save);
    }

    public ClientApp getClientAppByName(String appName) {
        if (StringUtils.isBlank(appName)) {
            return null;
        }

        if (cache.containsKey(appName)) {
            return cache.get(appName);
        }
        ClientApp clientApp = clientAppRepository.findById(appName).orElse(null);
        if (clientApp != null) {
            cache.put(appName, clientApp);
        }
        return clientApp;
    }

    public ClientApp getClientAppByUserId(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        NameRecord userIdRecord = NameRecord.parse(userId);
        if (!userIdRecord.isValid()) {
            return null;
        }

        return getClientAppByName(userIdRecord.getAppName());
    }

    public boolean isMasterUserId(String userId) {
        ClientApp clientApp = getClientAppByUserId(userId);
        return clientApp != null && StringUtils.equals(userId, clientApp.getMasterUserId());
    }

    public void saveClientApp(ClientApp clientApp) {
        clientAppRepository.save(clientApp);
    }

    public String resolveDomain(String userId) {
        String defaultDomainName = this.applicationConfig.getDomainName();

        ClientApp clientApp = this.getClientAppByUserId(userId);
        if (clientApp == null) {
            return defaultDomainName;
        }

        if (CollectionUtils.isEmpty(clientApp.getUserDistributions())) {
            return defaultDomainName;
        }

        NameRecord userIdRecord = NameRecord.parse(userId);
        if (!userIdRecord.isValid()) {
            return defaultDomainName;
        }

        final Long number = userIdRecord.getNumber();
        Optional<UserDistribution> distribution = clientApp.getUserDistributions().stream().filter(userDistribution -> {
            Long from = defaultValue(userDistribution.getFrom(), 0L);
            Long to = defaultValue(userDistribution.getTo(), Long.MAX_VALUE);
            return number >= from && number <= to;
        }).findFirst();

        if (distribution.isPresent()) {
            return distribution.get().getDomainName();
        } else {
            return defaultDomainName;
        }
    }
}
