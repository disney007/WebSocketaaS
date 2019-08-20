package com.linker.processor;

import com.linker.processor.express.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

import static com.linker.common.Utils.sleep;

@SpringBootApplication
@Slf4j
public class Application {

    @Autowired
    PostOffice postOffice;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutting down application");
        postOffice.stopIncoming();
        sleep(3000L);
        postOffice.stopOutging();
        sleep(3000L);
        log.info("shutdown complete");
    }
}

