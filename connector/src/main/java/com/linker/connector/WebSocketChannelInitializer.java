package com.linker.connector;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    ApplicationContext applicationContext;
    Long counter = 0L;

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        WebSocketHandler webSocketHandler = new WebSocketHandler(++counter);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(webSocketHandler);
        pipeline.addLast(webSocketHandler);
    }

    public void resetCounter() {
        counter = 0L;
    }
}
