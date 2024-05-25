package org.gms.net.netty;

import org.gms.client.Client;
import io.netty.channel.socket.SocketChannel;
import org.gms.net.PacketProcessor;
import org.gms.net.server.Server;
import org.gms.net.server.coordinator.session.SessionCoordinator;
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
        final String clientIp = socketChannel.remoteAddress().getHostString();
        log.info("客户端 {} 连接到了大区 {}, 频道 {}", clientIp, world, channel);

        PacketProcessor packetProcessor = PacketProcessor.getChannelServerProcessor(world, channel);
        final long clientSessionId = sessionId.getAndIncrement();
        final String remoteAddress = getRemoteAddress(socketChannel);
        final Client client = Client.createChannelClient(clientSessionId, remoteAddress, packetProcessor, world, channel);

        if (Server.getInstance().getChannel(world, channel) == null) {
            SessionCoordinator.getInstance().closeSession(client, true);
            socketChannel.close();
            return;
        }

        initPipeline(socketChannel, client);
    }
}
