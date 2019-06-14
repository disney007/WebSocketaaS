package com.linker.connector;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
@Slf4j
public class NettyService {

    @Slf4j
    static class NettyServerHandler extends ChannelInboundHandlerAdapter {

        public NettyServerHandler() {
            log.info("new client connected");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channel active {}", ctx);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.info("channel registered {}", ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.info("channel unregistered {}", ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("channel inactive {}", ctx);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.info("channel read complete {}", ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            log.info("user event triggered {}", ctx);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.info("channel writability changed {}", ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info(msg.toString());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("exception received {}", ctx, cause);
            ctx.close();
        }
    }

    @Slf4j
    static class NettyServer implements Runnable {

        volatile EventLoopGroup bossGroup;
        volatile EventLoopGroup workerGroup;

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
                        .childHandler(new WebSocketChannelInitializer())
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture f = b.bind(8089).sync();
                log.info("netty server started");
                f.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                log.error("error occurred in netty server", e);
            } finally {
                this.shutdown();
                log.info("netty server stopped");
            }
        }
    }

    NettyServer nettyServer = new NettyServer();

    public NettyService() {
        new Thread(nettyServer).start();
    }

    @PreDestroy
    void onDestroy() throws InterruptedException {
        nettyServer.shutdown();
    }
}
