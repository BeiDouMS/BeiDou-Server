package net.netty;

import config.YamlConfig;
import constants.net.OpcodeConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.mina.MapleCustomEncryption;
import net.packet.ByteBufInPacket;
import net.packet.OutPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.MapleAESOFB;

public class PacketEncoder extends MessageToByteEncoder<OutPacket> {
    private static final Logger log = LoggerFactory.getLogger(PacketEncoder.class);
    private static final boolean LOG_PACKETS = YamlConfig.config.server.USE_DEBUG_SHOW_PACKET;
    private final MapleAESOFB sendCypher;

    public PacketEncoder(MapleAESOFB sendCypher) {
        this.sendCypher = sendCypher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, OutPacket in, ByteBuf out) {
        byte[] packet = in.getBytes();
        out.writeBytes(getEncodedHeader(packet.length));

        if (LOG_PACKETS) {
            logPacket(packet);
        }

        MapleCustomEncryption.encryptData(packet);
        sendCypher.crypt(packet);
        out.writeBytes(packet);
    }

    private byte[] getEncodedHeader(int length) {
        return sendCypher.getPacketHeader(length);
    }

    private void logPacket(byte[] packet) {
        final int packetLength = packet.length;

        if (packetLength <= 50000) {
            final int opcode = readFirstShort(packet);
            String opcodeHex = Integer.toHexString(opcode).toUpperCase();
            String opcodeName = getSendOpcodeName(opcode);
            String prefix = opcodeName == null ? "<UnknownPacket> " : "";
            log.info("{}ServerSend:{} [{}] ({}) - hex:{} - text:{}", prefix, opcodeName, opcodeHex, packetLength,
                    HexTool.toString(packet), HexTool.toStringFromAscii(packet));
        } else {
            log.info(HexTool.toString(new byte[]{packet[0], packet[1]}) + " ...");
        }
    }

    private String getSendOpcodeName(int opcode) {
        return OpcodeConstants.sendOpcodeNames.get(opcode);
    }

    private static int readFirstShort(byte[] bytes) {
        return new ByteBufInPacket(Unpooled.wrappedBuffer(bytes)).readShort();
    }
}
