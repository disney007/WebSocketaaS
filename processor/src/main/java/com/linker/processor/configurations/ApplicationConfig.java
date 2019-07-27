package com.linker.processor.configurations;

import com.linker.processor.models.ClientApp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
    @Value("${kafkaHosts}")
    String kafkaHosts;
    @Value("${natsHosts}")
    String natsHosts;
    String consumerTopics;

    @Value("codec")
    String codec;

    List<ClientApp> clientApps = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("domain name = {}, processor name = {}", getDomainName(), getProcessorName());
    }
}
