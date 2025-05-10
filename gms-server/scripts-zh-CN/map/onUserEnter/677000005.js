function start(ms) {
    const Point = Java.type('java.awt.Point');
    var pos = new Point(201, 80);
    var mobId = 9400609;
    var mobName = "Andras";

    var player = ms.getPlayer();
    var map = player.getMap();

    if (map.getMonsterById(mobId) != null) {
        return;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const mobobj = LifeFactory.getMonster(mobId);
    mobName = mobobj.getName() || mobName;
    map.spawnMonsterOnGroundBelow(mobobj, pos);
    player.message(mobName + " 已现身！");
}