package net.netty;

import client.MapleClient;
import io.netty.channel.socket.SocketChannel;
import net.PacketProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerInitializer extends ServerChannelInitializer {
    private static final Logger log = LoggerFactory.getLogger(LoginServerInitializer.class);

    @Override
    public void initChannel(SocketChannel socketChannel) {
        final String clientIp = socketChannel.remoteAddress().getHostName();
        log.debug("Client connected to login server from {} ", clientIp);

        PacketProcessor packetProcessor = PacketProcessor.getLoginServerProcessor();
        final MapleClient client = new MapleClient(packetProcessor, LoginServer.WORLD, LoginServer.CHANNEL);
        client.setSessionId(sessionId.getAndIncrement());

        initPipeline(socketChannel, client);
    }
}
