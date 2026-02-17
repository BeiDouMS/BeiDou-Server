package org.gms.net.encryption.protocol;

import org.gms.net.encryption.ClientCyphers;

import java.util.HashMap;
import java.util.Map;

public class ProtocolFactory {
    private final Map<Short, PacketProtocol> PROTOCOLS = new HashMap<>();

    public ProtocolFactory(ClientCyphers clientCyphers){
        // 在这里注册版本与对应的处理器
        PROTOCOLS.put(ProtocolConstants.GMS_V83, new GMSV83PacketProtocol(clientCyphers));
    }

    public PacketProtocol getProtocol(short version) {
        PacketProtocol protocol = PROTOCOLS.get(version);

        if (protocol == null) {
            throw new UnsupportedOperationException("PacketProtocol is a unsupported version: " + version);
        }

        return protocol;
    }
}
