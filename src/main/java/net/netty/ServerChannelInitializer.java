package net.netty;

import client.MapleClient;
import constants.net.ServerConstants;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import tools.MaplePacketCreator;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    static final AtomicLong sessionId = new AtomicLong(7777);

    void initPipeline(SocketChannel socketChannel, MapleClient client) {
        final InitializationVector sendIv = InitializationVector.generateSend();
        final InitializationVector recvIv = InitializationVector.generateReceive();
        writeInitialUnencryptedHelloPacket(socketChannel, sendIv, recvIv);
        setUpHandlers(socketChannel.pipeline(), sendIv, recvIv, client);
    }

    private void writeInitialUnencryptedHelloPacket(SocketChannel socketChannel, InitializationVector sendIv, InitializationVector recvIv) {
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(MaplePacketCreator.getHello(ServerConstants.VERSION, sendIv, recvIv)));
    }

    private void setUpHandlers(ChannelPipeline pipeline, InitializationVector sendIv, InitializationVector recvIv,
                               MapleClient client) {
        pipeline.addFirst("IdleStateHandler", new IdleStateHandler(30, 30, 0));
        pipeline.addLast("PacketCodec", new PacketCodec(ClientCyphers.of(sendIv, recvIv)));
        pipeline.addLast("MapleClient", client);
    }
}
