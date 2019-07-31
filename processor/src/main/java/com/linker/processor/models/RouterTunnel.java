package com.linker.processor.models;

import com.linker.common.*;
import com.linker.common.exceptions.RouterNotConnectedException;
import com.linker.common.messages.AuthClient;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.network.TcpSocketClient;
import com.linker.common.router.Router;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.configurations.ApplicationConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Data
@Slf4j
public class RouterTunnel {
    final long RECONNECT_DELAY = 10;

    TcpSocketClient socketClient;
    Router router;
    boolean isAuthenticated = false;
    Consumer<MessageContentOutput> msgCallback;

    @Autowired
    ApplicationConfig applicationConfig;
    @Autowired
    ProcessorUtils processorUtils;

    public void connectRouter(Router router) {
        this.router = router;
        doConnect();
    }

    public void doConnect() {
        try {
            URI uri = new URI(this.router.getUrl());
            socketClient = new TcpSocketClient();

            log.info("start connecting to router {}", this.router);
            socketClient.connect(uri.getHost(), uri.getPort());
            socketClient.onConnected(this::onConnected);
            socketClient.onDisconnected(this::onDisconnected);
        } catch (URISyntaxException e) {
            log.error("invalid router url", e);
        }
    }

    void onConnected() {
        log.info("connected to router {}", this.router);
        socketClient.onMessage(this::onMessageArrived);
        authenticate();
    }

    void onDisconnected() {
        log.info("disconnected from router {}, reconnect in {} seconds", this.router, RECONNECT_DELAY);
        scheduleReconnection();
    }

    void authenticate() {
        this.isAuthenticated = false;
        final String domainName = applicationConfig.getDomainName();
        socketClient.sendMessage(MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient(
                processorUtils.resolveRouterAppName(domainName),
                getUserId(),
                ""
        ), MessageFeature.RELIABLE));
    }

    String getUserId() {
        return processorUtils.resolveRouterUserId(applicationConfig.getDomainName());
    }

    void handleAuthReplay(AuthClientReply reply) {
        if (reply.getIsAuthenticated()) {
            log.info("[{}] is authenticated to router {}", getUserId(), this.router);
            this.isAuthenticated = true;
        } else {
            log.warn("[{}] is failed to authenticate to router {}, disconnect", getUserId(), this.router);
            this.socketClient.disconnect();
        }
    }

    void scheduleReconnection() {
        processorUtils.getScheduledExecutorService().schedule(() -> this.socketClient.reconnect(), RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    void onMessageArrived(MessageContentOutput content) {
        switch (content.getType()) {
            case AUTH_CLIENT_REPLY:
                this.handleAuthReplay(content.getData(AuthClientReply.class));
                return;
            default:
                if (this.msgCallback != null) {
                    this.msgCallback.accept(content);
                } else {
                    log.info("message [{}] received from router [{}], ignore", content.getType(), this.getName());
                }
        }
    }

    public void onMessage(Consumer<MessageContentOutput> msgCallback) {
        this.msgCallback = msgCallback;
    }

    public String getName() {
        return this.router.getName();
    }

    public boolean isConnectedToRouter() {
        return this.socketClient != null && isAuthenticated;
    }

    public void sendMessage(Message message) throws RouterNotConnectedException {
        if (!this.isConnectedToRouter()) {
            throw new RouterNotConnectedException("domain is not connected to router " + this.router);
        }
        socketClient.sendMessage(MessageUtils.createMessageContent(MessageType.INTERNAL_MESSAGE, message, message.getContent().getFeature()));
    }
}
