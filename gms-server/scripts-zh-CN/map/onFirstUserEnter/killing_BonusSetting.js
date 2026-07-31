function start(ms) {
    var player = ms.getPlayer();
    var map = player.getMap();

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    const PacketCreator = Java.type('org.gms.util.PacketCreator');

    var mobId = (map.getId() == 926010013 || map.getId() == 926010070) ? 9700029 : 9700019;
    var spawnX = [-360, -280, -200, -120, -40, 40, 120, 200, 280, 360];

    map.killAllMonsters();
    map.broadcastMessage(PacketCreator.getClock(60));
    ms.scheduleWarpMap(60, map.getReturnMapId());
    player.resetEnteredScript(map.getId());
    for (var i = 0; i < spawnX.length; i++) {
        map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(mobId), new Point(spawnX[i], 100));
    }
}
