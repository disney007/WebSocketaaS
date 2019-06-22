package com.linker.processor.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
public class ApplicationConfig {
    String domainName;
    String processorName;

    String rabbitmqHosts;
    String kafkaHosts;
    String natsHosts;
    String consumerTopics;
}
