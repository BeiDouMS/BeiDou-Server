package net.netty;

import client.MapleClient;
import io.netty.channel.socket.SocketChannel;
import net.PacketProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(ChannelServerInitializer.class);

    private final int world;
    private final int channel;

    public ChannelServerInitializer(int world, int channel) {
        this.world = world;
        this.channel = channel;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostName();
        log.debug("Client connected to world {}, channel {} from {}", world, channel, clientIp);

        PacketProcessor packetProcessor = PacketProcessor.getChannelServerProcessor(world, channel);
        final MapleClient client = new MapleClient(packetProcessor, world, channel);
        client.setSessionId(sessionId.getAndIncrement());

        initPipeline(socketChannel, client);
    }
}
