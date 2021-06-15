package net.netty;

import constants.net.ServerConstants;
import tools.MapleAESOFB;

public class ClientCyphers {
    private final MapleAESOFB send;
    private final MapleAESOFB receive;

    private ClientCyphers(MapleAESOFB send, MapleAESOFB receive) {
        this.send = send;
        this.receive = receive;
    }

    public static ClientCyphers generateNew() {
        MapleAESOFB send = new MapleAESOFB(InitializationVector.generateSend(), ServerConstants.VERSION);
        MapleAESOFB receive = new MapleAESOFB(InitializationVector.generateReceive(), ServerConstants.VERSION);
        return new ClientCyphers(send, receive);
    }

    public MapleAESOFB getSendCypher() {
        return send;
    }

    public MapleAESOFB getReceiveCypher() {
        return receive;
    }
}
