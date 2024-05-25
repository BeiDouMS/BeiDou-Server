package org.gms.netty;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.dto.BaseNettyRequest;
import org.gms.dto.BaseNettyResponse;
import org.gms.property.ServerProperty;
import org.gms.util.MapleAESOFB;
import org.gms.util.PacketDecoder;
import org.gms.util.PacketEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class NettyClient {
    private final ServerProperty serverProperty;

    public <T, K> BaseNettyResponse<K> sendJson(BaseNettyRequest<T> request) {
        StringBuffer msgBuffer = new StringBuffer();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                        socketChannel.pipeline().addLast(new NettyClientHandler(msgBuffer));
                    }
                });
        try {
            Channel channel = bootstrap.connect(serverProperty.getHost(), serverProperty.getPort()).sync().channel();
            String requestMsg = JSONObject.toJSONString(request);
            log.info("send msg:{}", requestMsg);
            channel.writeAndFlush(requestMsg).sync();
            channel.closeFuture().await();
            log.info("receive msg:{}", msgBuffer);
            return JSONObject.parseObject(msgBuffer.toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("send msg error:{}", e.getMessage(), e);
            return BaseNettyResponse.<K>builder()
                    .responseId(request.getRequestId())
                    .responseCode("error")
                    .responseMessage(e.getMessage())
                    .build();
        } finally {
            group.shutdownGracefully();
        }

    }

    public void sendPacket(byte[] request) {
        byte[] sendIv = new byte[4];
        byte[] recvIv = new byte[4];
        EventLoopGroup group = new NioEventLoopGroup();
        Channel channel;
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                                ByteBuf byteBuf = (ByteBuf) msg;
                                byteBuf.readShortLE();
                                byteBuf.readShortLE();
                                byteBuf.readShortLE();
                                byteBuf.readByte();
                                byteBuf.readBytes(sendIv);
                                byteBuf.readBytes(recvIv);
                                ctx.close();
                            }
                        });
                    }
                });

        try {
            channel = bootstrap.connect(serverProperty.getHost(), 8484).sync().channel();
            channel.closeFuture().await();
        } catch (Exception e) {
            log.error("send msg error:{}", e.getMessage(), e);
        }
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new PacketEncoder(new MapleAESOFB(sendIv, (short) 83)));
                socketChannel.pipeline().addLast(new PacketDecoder(new MapleAESOFB(recvIv, (short) 83)));
//                socketChannel.pipeline().addLast(new CombinedChannelDuplexHandler<>(
//                        new PacketDecoder(new MapleAESOFB(recvIv, (short) 83)),
//                        new PacketEncoder(new MapleAESOFB(sendIv, (short) 83))));

                socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        super.channelRead(ctx, msg);
                        ctx.close();
                    }
                });
            }
        });
        try {
            channel = bootstrap.connect(serverProperty.getHost(), 8484).sync().channel();
            channel.writeAndFlush(Unpooled.wrappedBuffer(request)).sync();
            channel.closeFuture().await();
        } catch (Exception e) {
            log.error("send msg error:{}", e.getMessage(), e);
        }
        group.shutdownGracefully();

    }
}
