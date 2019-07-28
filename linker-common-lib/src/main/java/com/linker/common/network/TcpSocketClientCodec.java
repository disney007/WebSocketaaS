package com.linker.common.network;

import com.linker.common.MessageContent;
import com.linker.common.MessageContentOutput;
import com.linker.common.codec.Codec;
import com.linker.common.codec.FstCodec;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class TcpSocketClientCodec extends MessageToMessageCodec<byte[], MessageContent> {
    private static Codec codec = new FstCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageContent msg, List<Object> out) throws Exception {
        out.add(codec.serialize(msg));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
        out.add(codec.deserialize(msg, MessageContentOutput.class));
    }
}
