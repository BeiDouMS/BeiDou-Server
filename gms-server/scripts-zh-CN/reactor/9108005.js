function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
        var react = rm.getReactor().getMap().getReactorByName("fullmoon");
        var stage = parseInt(eim.getProperty("stage")) + 1;
        var newStage = stage.toString();
        eim.setProperty("stage", newStage);
        react.forceHitReactor(react.getState() + 1);
        if (eim.getProperty("stage") === "6") {
            rm.mapMessage(6, "月妙开始制作美味的年糕，其香味会吸引各种怪物，请务必守护好月妙！！！");
            var map = eim.getMapInstance(rm.getReactor().getMap().getId());
            map.allowSummonState(true);
            map.spawnMonsterOnGroundBelow(9300061, -183, -433);
        }
    }
}