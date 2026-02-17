package org.gms.net.encryption;

import io.netty.channel.CombinedChannelDuplexHandler;
import org.gms.net.encryption.protocol.ProtocolFactory;

public class PacketCodec extends CombinedChannelDuplexHandler<PacketDecoder, PacketEncoder> {
    public PacketCodec(ProtocolFactory protocolFactory) {
        super(new PacketDecoder(protocolFactory), new PacketEncoder(protocolFactory));
    }
}
