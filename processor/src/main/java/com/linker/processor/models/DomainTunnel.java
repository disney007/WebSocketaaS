package com.linker.processor.models;

import com.linker.common.*;
import com.linker.common.exceptions.NotConnectedException;
import com.linker.common.messages.AuthClient;
import com.linker.common.messages.AuthClientReply;
import com.linker.common.network.TcpSocketClient;
import com.linker.common.router.Domain;
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
public class DomainTunnel {
    final long RECONNECT_DELAY = 10;

    TcpSocketClient socketClient;
    Domain domain;
    boolean isAuthenticated = false;
    Consumer<MessageContentOutput> msgCallback;

    @Autowired
    ApplicationConfig applicationConfig;
    @Autowired
    ProcessorUtils processorUtils;

    public void connectDomain(Domain domain) {
        this.domain = domain;
        doConnect();
    }

    public void doConnect() {
        try {
            String url = this.domain.getTcpUrl();
            if (url == null) {
                log.warn("tcp url is not provided in domain {}, skip connecting", domain);
                return;
            }

            URI uri = new URI(url);
            socketClient = new TcpSocketClient();

            log.info("start connecting to domain [{}]", this.domain);
            socketClient.connect(uri.getHost(), uri.getPort());
            socketClient.onConnected(this::onConnected);
            socketClient.onDisconnected(this::onDisconnected);
        } catch (URISyntaxException e) {
            log.error("invalid domain url", e);
        }
    }

    void onConnected() {
        log.info("connected to domain [{}] from current domain [{}]", this.domain, applicationConfig.getDomainName());
        socketClient.onMessage(this::onMessageArrived);
        authenticate();
    }

    void onDisconnected() {
        log.info("disconnected from domain [{}], reconnect in {} seconds", this.domain, RECONNECT_DELAY);
        scheduleReconnection();
    }

    void authenticate() {
        this.isAuthenticated = false;
        final String domainName = applicationConfig.getDomainName();
        final String userId = getUserId();
        log.info("authenticating user [{}] to [{}]", userId, this.domain);

        socketClient.sendMessage(MessageUtils.createMessageContent(MessageType.AUTH_CLIENT, new AuthClient(
                processorUtils.resolveDomainAppName(domainName),
                userId,
                ""
        ), MessageFeature.RELIABLE));
    }

    String getUserId() {
        return processorUtils.resolveDomainUserId(applicationConfig.getDomainName());
    }

    void handleAuthReplay(AuthClientReply reply) {
        if (reply.getIsAuthenticated()) {
            log.info("current user [{}] is authenticated to domain [{}]", getUserId(), this.domain);
            this.isAuthenticated = true;
        } else {
            log.warn("current user [{}] is failed to authenticate to domain {}, disconnect", getUserId(), this.domain);
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
                    log.info("message [{}] received from domain [{}], ignore", content.getType(), this.getName());
                }
        }
    }

    public void onMessage(Consumer<MessageContentOutput> msgCallback) {
        this.msgCallback = msgCallback;
    }

    public String getName() {
        return this.domain.getName();
    }

    public boolean isConnected() {
        return this.socketClient != null && isAuthenticated;
    }

    public void sendMessage(Message message) throws NotConnectedException {
        if (!this.isConnected()) {
            throw new NotConnectedException("domain is not connected to domain " + this.domain);
        }
        socketClient.sendMessage(MessageUtils.createMessageContent(MessageType.INTERNAL_MESSAGE, message, message.getContent().getFeature()));
    }
}
