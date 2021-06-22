package net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.mina.MapleCustomEncryption;
import net.packet.OutPacket;
import tools.MapleAESOFB;

public class PacketEncoder extends MessageToByteEncoder<OutPacket> {
    private final MapleAESOFB sendCypher;

    public PacketEncoder(MapleAESOFB sendCypher) {
        this.sendCypher = sendCypher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, OutPacket in, ByteBuf out) {
        byte[] packet = in.getBytes();
        out.writeBytes(getEncodedHeader(packet.length));
        MapleCustomEncryption.encryptData(packet);
        sendCypher.crypt(packet);
        out.writeBytes(packet);
    }

    private byte[] getEncodedHeader(int length) {
        return sendCypher.getPacketHeader(length);
    }
}
