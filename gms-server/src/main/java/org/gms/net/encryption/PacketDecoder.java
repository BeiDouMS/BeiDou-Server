package org.gms.net.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.gms.constants.net.ServerConstants;
import org.gms.net.encryption.protocol.ProtocolFactory;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<Void> {
    private final ProtocolFactory protocolFactory;

    public PacketDecoder(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        protocolFactory.getProtocol(ServerConstants.VERSION).decode(context, in, out);
    }
}
