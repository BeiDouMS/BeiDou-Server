package org.gms.net.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.gms.api.ApiServerHandler;

public class ApiServer extends AbstractServer{
    private Channel channel;

    public ApiServer(int port) {
        super(port);
    }

    @Override
    public void start() {
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                        socketChannel.pipeline().addLast(new ApiServerHandler());
                    }
                });

        this.channel = bootstrap.bind(port).syncUninterruptibly().channel();
    }

    @Override
    public void stop() {
        if (channel == null) {
            throw new IllegalStateException("Must start LoginServer before stopping it");
        }

        channel.close().syncUninterruptibly();
    }
}
