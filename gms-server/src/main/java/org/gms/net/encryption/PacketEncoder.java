package org.gms.net.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.gms.constants.net.ServerConstants;
import org.gms.net.encryption.protocol.ProtocolFactory;
import org.gms.net.packet.Packet;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final ProtocolFactory protocolFactory;

    public PacketEncoder(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet in, ByteBuf out) {
        protocolFactory.getProtocol(ServerConstants.VERSION).encode(ctx, in, out);
    }
}
