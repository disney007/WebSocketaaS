package com.linker.processor.configurations;

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
    @Value("${processorName}")
    String processorName;

    String rabbitmqHosts;
    String kafkaHosts;
    String natsHosts;
    String consumerTopics;

    @PostConstruct
    public void init() {
        log.info("domain name = {}, processor name = {}", getDomainName(), getProcessorName());
    }
}
