package com.linker.meta.configurations;

import com.linker.common.client.ClientApp;
import com.linker.common.router.Router;
import com.linker.common.router.RouterLink;
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

    List<Router> routers = new ArrayList<>();
    Set<RouterLink> routerLinks = new HashSet<>();
    List<ClientApp> clientApps = new ArrayList<>();
}
