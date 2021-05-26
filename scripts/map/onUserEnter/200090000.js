// Author: Ronan
var mapId = 200090000;

function start(ms) {
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

	if(map.getDocked()) {
		const MaplePacketCreator = Java.type('tools.MaplePacketCreator');
		ms.getClient().announce(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
		ms.getClient().announce(MaplePacketCreator.crogBoatPacket(true));
	}

	return true;
}