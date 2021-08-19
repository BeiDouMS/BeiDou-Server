// Author: Ronan
var mapId = 200090010;

function start(ms) {
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

	if(map.getDocked()) {
		const PacketCreator = Java.type('tools.PacketCreator');
		ms.getClient().announce(PacketCreator.musicChange("Bgm04/ArabPirate"));
		ms.getClient().announce(PacketCreator.crogBoatPacket(true));
	}

	return true;
}