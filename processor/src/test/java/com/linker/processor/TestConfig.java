package com.linker.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linker.common.Message;
import com.linker.common.client.ClientApp;
import com.linker.common.codec.Codec;
import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
import com.linker.common.router.Domain;
import com.linker.common.router.DomainGraph;
import com.linker.processor.services.MetaServerService;
import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.mockito.Mockito.*;

@Configuration
@Slf4j
public class TestConfig {

    String host = "localhost";

    int mongoPort = 37017;

    @Autowired
    RedisServer redisServer;

    @Autowired
    MongodExecutable embeddedMongoServer;

    @Autowired
    Mongo mongo;

    @Autowired
    Codec codec;

    static TestConfig instance;

    public TestConfig() {
        instance = this;
    }

    @Bean
    public RedisServer redisServer() {
        log.info("start embedded redis server");
        RedisServer redisServer = new RedisServer(16379);
        redisServer.start();
        return redisServer;
    }

    @Bean
    public MongodExecutable embeddedMongoServer() throws IOException {
        log.info("start embedded mongo server");
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(host, mongoPort, false))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        return mongodExecutable;
    }

    @PreDestroy
    public void clean() {
        log.info("shut down redis server");
        redisServer.stop();
        log.info("close mongo client");
        mongo.close();
    }

    @Bean
    public MockKafkaExpressDelivery kafkaExpressDelivery() {
        return spy(new MockKafkaExpressDelivery(codec));
    }

    @Bean
    public MockNatsExpressDelivery natsExpressDelivery() {
        return spy(new MockNatsExpressDelivery(codec));
    }

    @Bean
    @Primary
    public MetaServerService metaServerService() throws IOException {
        MetaServerService serverService = mock(MetaServerService.class);
        when(serverService.getClientApps()).thenReturn(ImmutableList.of());

        DomainGraph graph = new DomainGraph();
        Domain domain = new Domain();
        domain.setName("domain-01");
        graph.setDomains(ImmutableList.of(domain));
        graph.setLinks(ImmutableSet.of());
        when(serverService.getDomainGraph()).thenReturn(graph);
        return serverService;
    }
}
