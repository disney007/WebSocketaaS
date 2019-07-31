package com.linker.meta.services;

import com.google.common.collect.Lists;
import com.linker.common.client.ClientApp;
import com.linker.meta.configurations.ApplicationConfig;
import com.linker.meta.repositories.ClientAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientAppService {
    final ApplicationConfig applicationConfig;
    final ClientAppRepository clientAppRepository;

    @PostConstruct
    public void loadDefaultData() {
        log.info("load default client apps");
        applicationConfig.getClientApps().forEach(clientAppRepository::save);
    }

    public List<ClientApp> getAllClientApps() {
        return Lists.newArrayList(clientAppRepository.findAll());
    }
}
