package com.linker.processor.services;

import com.linker.processor.configurations.ApplicationConfig;
import com.linker.processor.models.ClientApp;
import com.linker.processor.repositories.ClientAppRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ClientAppService {

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    ClientAppRepository clientAppRepository;

    @PostConstruct
    public void init() {
        log.info("load client apps from application config");
        applicationConfig.getClientApps().forEach(app -> clientAppRepository.save(app));
    }

    public ClientApp getClientAppByName(String appName) {
        if (StringUtils.isBlank(appName)) {
            return null;
        }

        return clientAppRepository.findById(appName).orElse(null);
    }

    public ClientApp getClientAppByUserId(String userId) {
        String appName = userId.split("-")[0];
        return getClientAppByName(appName);
    }

    public void saveClientApp(ClientApp clientApp) {
        clientAppRepository.save(clientApp);
    }
}
