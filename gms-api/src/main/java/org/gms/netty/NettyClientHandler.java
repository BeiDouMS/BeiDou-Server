package org.gms.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private final StringBuffer msgBuffer;
    public NettyClientHandler(StringBuffer msgBuffer) {
        this.msgBuffer = msgBuffer;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        msgBuffer.append(msg);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client exception", cause);
        ctx.close();
    }
}
