package org.gms.util;


public class ClientCyphers {
    private final MapleAESOFB send;
    private final MapleAESOFB receive;

    private ClientCyphers(MapleAESOFB send, MapleAESOFB receive) {
        this.send = send;
        this.receive = receive;
    }

    public static ClientCyphers of(byte[] sendIv, byte[] receiveIv) {
        MapleAESOFB send = new MapleAESOFB(sendIv, (short) (0xFFFF - 83));
        MapleAESOFB receive = new MapleAESOFB(receiveIv, (short) 83);
        return new ClientCyphers(send, receive);
    }

    public MapleAESOFB getSendCypher() {
        return send;
    }

    public MapleAESOFB getReceiveCypher() {
        return receive;
    }
}
