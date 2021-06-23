package net.netty;

import client.MapleClient;
import constants.net.ServerConstants;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import tools.MaplePacketCreator;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    static final AtomicLong sessionId = new AtomicLong(7777);

    void initPipeline(SocketChannel socketChannel, MapleClient client) {
        final InitializationVector sendIv = InitializationVector.generateSend();
        final InitializationVector recvIv = InitializationVector.generateReceive();
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(MaplePacketCreator.getHello(ServerConstants.VERSION, sendIv, recvIv)));
        socketChannel.pipeline().addLast("PacketCodec", new PacketCodec(ClientCyphers.of(sendIv, recvIv)));
        socketChannel.pipeline().addLast("MapleClient", client);
    }
}
