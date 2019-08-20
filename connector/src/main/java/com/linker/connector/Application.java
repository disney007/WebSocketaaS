package com.linker.connector;

import com.linker.connector.express.PostOffice;
import com.linker.connector.network.NettyService;
import com.linker.connector.network.SocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

import static com.linker.common.Utils.sleep;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Autowired
    NetworkUserService networkUserService;

    @Autowired
    NettyService nettyService;

    @Autowired
    PostOffice postOffice;

    @PreDestroy
    void shutdown() {
        log.info("shutting down application");
        postOffice.stopIncoming();
        sleep(3000L);
        networkUserService.users.forEach((key, value) -> value.forEach(SocketHandler::close));
        sleep(3000L);
        nettyService.shutdown();
        sleep(3000L);
        postOffice.stopOutging();
        sleep(3000L);
        log.info("shutdown complete");
    }
}
