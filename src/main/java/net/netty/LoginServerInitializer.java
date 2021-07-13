package net.netty;

import client.MapleClient;
import io.netty.channel.socket.SocketChannel;
import net.PacketProcessor;
import net.server.coordinator.session.MapleSessionCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostString();
        log.debug("Client connected to login server from {} ", clientIp);

        PacketProcessor packetProcessor = PacketProcessor.getLoginServerProcessor();
        final MapleClient client = new MapleClient(MapleClient.Type.LOGIN, packetProcessor, LoginServer.WORLD_ID, LoginServer.CHANNEL_ID);
        client.setSessionId(sessionId.getAndIncrement());

        if (!MapleSessionCoordinator.getInstance().canStartLoginSession(client)) {
            socketChannel.close();
            return;
        }

        initPipeline(socketChannel, client);
    }
}
