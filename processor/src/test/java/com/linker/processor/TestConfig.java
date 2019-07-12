package com.linker.processor;

import com.linker.common.messagedelivery.MockKafkaExpressDelivery;
import com.linker.common.messagedelivery.MockNatsExpressDelivery;
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
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.mockito.Mockito.spy;

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

    public void clean() {
        log.info("shut down redis server");
        redisServer.stop();
        log.info("close mongo client");
        mongo.close();
    }

    @Bean
    public MockKafkaExpressDelivery kafkaExpressDelivery() {
        return spy(new MockKafkaExpressDelivery());
    }

    @Bean
    public MockNatsExpressDelivery natsExpressDelivery() {
        return spy(new MockNatsExpressDelivery());
    }
}
