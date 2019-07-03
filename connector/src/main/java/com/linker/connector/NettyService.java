package com.linker.connector;

import com.linker.connector.configurations.ApplicationConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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

@Service
@Slf4j
public class NettyService {
    public interface NettyServiceListener {
        void onNettyServiceStarted();
    }

    @Setter
    NettyServiceListener listener;

    @Slf4j
    static class NettyServer implements Runnable {

        @Autowired
        ApplicationConfig applicationConfig;

        @Autowired
        WebSocketChannelInitializer channelInitializer;

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
                ChannelFuture f = b.bind(applicationConfig.getWsPort()).sync();
                log.info("netty server started");
                nettyService.fireNettyServerStartedEvent();
                f.channel().closeFuture().sync();

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
        new Thread(nettyServer).start();
    }

    void shutdown() {
        nettyServer.shutdown();
    }
}
