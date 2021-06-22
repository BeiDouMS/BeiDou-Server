package net.netty;

import client.MapleClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger log = LoggerFactory.getLogger(ClientInitializer.class);

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostName();
        log.debug("Client initiated new connection from: {}", clientIp);

        socketChannel.pipeline().addLast("PacketCodec", new PacketCodec(ClientCyphers.generateNew()));
        socketChannel.pipeline().addLast("MapleClient", new MapleClient());
    }
}
