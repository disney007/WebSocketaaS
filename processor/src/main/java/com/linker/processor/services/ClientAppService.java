package com.linker.processor.services;

import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.models.ClientApp;
import com.linker.processor.repositories.ClientAppRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ClientAppService {

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    ClientAppRepository clientAppRepository;

    ConcurrentHashMap<String, ClientApp> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("load client apps from application config");
        cache.clear();
        applicationConfig.getClientApps().forEach(app -> clientAppRepository.save(app));
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
        String appName = userId.split("-")[0];
        return getClientAppByName(appName);
    }

    public boolean isMasterUserId(String userId) {
        ClientApp clientApp = getClientAppByUserId(userId);
        return clientApp != null && StringUtils.equals(userId, clientApp.getMasterUserId());
    }

    public void saveClientApp(ClientApp clientApp) {
        clientAppRepository.save(clientApp);
    }
}
