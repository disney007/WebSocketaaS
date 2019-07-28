package com.linker.connector.network;

import com.linker.connector.configurations.ApplicationConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NettyService {
    public interface NettyServiceListener {
        void onNettyServiceStarted();
    }

    @Setter
    NettyServiceListener listener;

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Slf4j
    static class NettyServer implements Runnable {

        @Autowired
        ApplicationConfig applicationConfig;

        @Autowired
        SocketChannelInitializer channelInitializer;

        volatile EventLoopGroup bossGroup;
        volatile EventLoopGroup workerGroup;
        NettyService nettyService;

        NettyServer(NettyService nettyService) {
            this.nettyService = nettyService;
        }

        public void shutdown() {
            log.info("shutdown netty server");
            if (workerGroup != null) {
                Future<?> future = workerGroup.shutdownGracefully();
                future.syncUninterruptibly();
                workerGroup = null;
            }
            if (bossGroup != null) {
                Future<?> future = bossGroup.shutdownGracefully();
                future.syncUninterruptibly();
                bossGroup = null;
            }
        }

        public void run() {
            log.info("starting netty server");
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(channelInitializer)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                List<Channel> channels = new ArrayList<>();
                if (applicationConfig.getWsPort() != null) {
                    log.info("starting web socket channel");
                    channels.add(b.bind(applicationConfig.getWsPort()).sync().channel());
                }

                if (applicationConfig.getTcpPort() != null) {
                    log.info("starting tcp channel");
                    channels.add(b.bind(applicationConfig.getTcpPort()).sync().channel());
                }

                if (channels.size() == 0) {
                    log.warn("no channel open");
                } else {
                    log.info("netty server started");
                    nettyService.fireNettyServerStartedEvent();
                    for (Channel channel : channels) {
                        channel.closeFuture().sync();
                    }
                }
            } catch (InterruptedException e) {
                log.error("error occurred in netty server", e);
            } finally {
                this.shutdown();
                log.info("netty server stopped");
            }
        }
    }

    NettyServer nettyServer;

    void fireNettyServerStartedEvent() {
        if (this.listener != null) {
            this.listener.onNettyServiceStarted();
        }
    }

    @Autowired
    public NettyService(ApplicationContext context) {
        nettyServer = new NettyServer(this);
        context.getAutowireCapableBeanFactory().autowireBean(nettyServer);
    }

    @PostConstruct
    void start() {
        executorService.schedule(nettyServer, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        nettyServer.shutdown();
    }
}
