package com.linker.meta.configurations;

import com.linker.common.client.ClientApp;
import com.linker.common.router.Domain;
import com.linker.common.router.DomainLink;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
@Slf4j
public class ApplicationConfig {

    List<Domain> domains = new ArrayList<>();
    Set<DomainLink> domainLinks = new HashSet<>();
    List<ClientApp> clientApps = new ArrayList<>();
}
