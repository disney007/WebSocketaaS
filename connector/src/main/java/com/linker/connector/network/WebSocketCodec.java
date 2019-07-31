package com.linker.connector.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import com.linker.common.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WebSocketCodec extends MessageToMessageCodec<TextWebSocketFrame, MessageContentOutput> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContentOutput messageContent, List<Object> out) throws Exception {
        out.add(new TextWebSocketFrame(Utils.toJson(messageContent)));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out) throws Exception {
        out.add(Utils.fromJson(msg.text(), MessageContent.class));
    }
}
