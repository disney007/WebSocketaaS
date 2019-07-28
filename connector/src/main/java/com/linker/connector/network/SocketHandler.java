package com.linker.connector.network;

import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.connector.AuthStatus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public interface SocketHandler {
    ChannelFuture sendMessage(MessageContent message);

    ChannelFuture sendMessage(Message message);


    void setUserId(String userId);

    String getUserId();

    AuthStatus getAuthStatus();

    void setAuthStatus(AuthStatus authStatus);

    ChannelFuture close();

    Long getSocketId();

    Channel getChannel();
}
