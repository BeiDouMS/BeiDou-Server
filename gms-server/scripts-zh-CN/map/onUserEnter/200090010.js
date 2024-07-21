// Author: Ronan
var mapId = 200090010;

function start(ms) {
    var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

    if (map.getDocked()) {
        const PacketCreator = Java.type('org.gms.util.PacketCreator');
        ms.getClient().sendPacket(PacketCreator.musicChange("Bgm04/ArabPirate"));
        ms.getClient().sendPacket(PacketCreator.crogBoatPacket(true));
    }

    return true;
}