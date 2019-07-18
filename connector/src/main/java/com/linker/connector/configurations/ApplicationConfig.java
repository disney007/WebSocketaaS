package com.linker.connector.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
@Slf4j
public class ApplicationConfig {
    @Value("${domainName}")
    String domainName;
    @Value("${connectorName}")
    String connectorName;

    @Value("${wsPort}")
    Integer wsPort;
    String rabbitmqHosts;
    @Value("${kafkaHosts}")
    String kafkaHosts;
    @Value("${natsHosts}")
    String natsHosts;
    String deliveryTopics;


    @PostConstruct
    public void init() {
        log.info("domain name = {}, connector name = {}", getDomainName(), getConnectorName());
    }
}
