package com.linker.connector.network;

import com.linker.connector.configurations.ApplicationConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    // the max length of message in binary
    private final static Integer MAX_CONTENT_LENGTH = 8192;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ApplicationConfig applicationConfig;

    Long counter = 0L;

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        Integer port = ch.localAddress().getPort();
        if (port.equals(applicationConfig.getWsPort())) {
            buildWebSocketPipeline(pipeline);
        } else if (port.equals(applicationConfig.getTcpPort())) {
            buildTcpSocketPipeline(pipeline);
        }

        AppSocketHandler appSocketHandler = new AppSocketHandler(++counter);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(appSocketHandler);
        pipeline.addLast(appSocketHandler);
    }

    public void resetCounter() {
        counter = 0L;
    }

    void buildWebSocketPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new WebSocketCodec());
    }

    void buildTcpSocketPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_CONTENT_LENGTH, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new ByteArrayDecoder());
        pipeline.addLast(new ByteArrayEncoder());
        pipeline.addLast(new TcpSocketCodec());
    }
}
