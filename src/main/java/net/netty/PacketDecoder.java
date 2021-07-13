package net.netty;

import config.YamlConfig;
import constants.net.OpcodeConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import net.mina.MapleCustomEncryption;
import net.packet.ByteBufInPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.MapleAESOFB;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<Void> {
    private static final Logger log = LoggerFactory.getLogger(PacketDecoder.class);
    private static final boolean LOG_PACKETS = YamlConfig.config.server.USE_DEBUG_SHOW_PACKET;
    private final MapleAESOFB receiveCypher;

    public PacketDecoder(MapleAESOFB receiveCypher) {
        this.receiveCypher = receiveCypher;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        final int header = in.readInt();

        if (!receiveCypher.isValidHeader(header)) {
            throw new InvalidPacketHeaderException("Attempted to decode a packet with an invalid header", header);
        }

        final int packetLength = decodePacketLength(header);
        byte[] packet = new byte[packetLength];
        in.readBytes(packet);
        receiveCypher.crypt(packet);
        MapleCustomEncryption.decryptData(packet);
        out.add(new ByteBufInPacket(Unpooled.wrappedBuffer(packet)));

        if (LOG_PACKETS){
            logPacket(packet);
        }
    }

    /**
     * @param header Packet header - the first 4 bytes of the packet
     * @return Packet size in bytes
     */
    private static int decodePacketLength(byte[] header) {
        return (((header[1] ^ header[3]) & 0xFF) << 8) | ((header[0] ^ header[2]) & 0xFF);
    }

    private static int decodePacketLength(int header) {
        int length = ((header >>> 16) ^ (header & 0xFFFF));
        length = ((length << 8) & 0xFF00) | ((length >>> 8) & 0xFF);
        return length;
    }

    private void logPacket(byte[] packet) {
        final int packetLength = packet.length;

        if (packetLength <= 3000) {
            final int opcode = readFirstShort(packet);
            final String opcodeHex = Integer.toHexString(opcode).toUpperCase();
            final String opcodeName = getRecvOpcodeName(opcode);
            final String prefix = opcodeName == null ? "<UnknownPacket> " : "";
            log.info("{}ClientSend:{} [{}] ({}) - hex:{} - text:{}", prefix, opcodeName, opcodeHex, packetLength,
                    HexTool.toString(packet), HexTool.toStringFromAscii(packet));
        } else {
            log.debug(HexTool.toString(new byte[]{packet[0], packet[1]}) + "...");
        }
    }

    private static int readFirstShort(byte[] bytes) {
        return new ByteBufInPacket(Unpooled.wrappedBuffer(bytes)).readShort();
    }

    private String getRecvOpcodeName(int opcode) {
        return OpcodeConstants.recvOpcodeNames.get(opcode);
    }
}
