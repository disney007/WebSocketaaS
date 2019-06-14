package com.linker.connector;

import com.linker.common.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text();

            Message.ClientMessageRequest clientMessageRequest = Utils.fromJson(text, Message.ClientMessageRequest.class);
            log.info("msg received {}", clientMessageRequest);
            ctx.write(new TextWebSocketFrame(text + "- reply"));
            ctx.flush();
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("channel added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("channel removed");
    }
}
