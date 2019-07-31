package com.linker.connector.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import com.linker.common.codec.Codec;
import com.linker.common.codec.FstCodec;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TcpSocketCodec extends MessageToMessageCodec<byte[], MessageContentOutput> {

    private static Codec codec = new FstCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContentOutput messageContent, List<Object> out) throws Exception {
        out.add(codec.serialize(messageContent));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        out.add(codec.deserialize(msg, MessageContent.class));
    }
}
