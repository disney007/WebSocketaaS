package com.linker.connector;

import com.linker.connector.network.NettyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class IntegrationTestEnv implements NettyService.NettyServiceListener {
    CompletableFuture<Object> future = new CompletableFuture<>();

    @Autowired
    public IntegrationTestEnv(NettyService nettyService) {
        nettyService.setListener(this);
    }

    @Override
    public void onNettyServiceStarted() {
        this.future.complete(null);
    }

    public void waitForReady() {
        try {
            future.get(30L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("error occurred", e);
        }
    }
}
