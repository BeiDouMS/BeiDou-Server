var leverSequenceExit = false;

function enterLeverSequence(pi) {
    var map = pi.getMap();

    var jailn = (pi.getMap().getId() / 10) % 10;
    var maxToggles = (jailn == 1) ? 7 : 6;

    var mapProp = pi.getEventInstance().getProperty("jail" + jailn);

    if (mapProp == null) {
        var seq = 0;

        for (var i = 1; i <= maxToggles; i++) {
            if (Math.random() < 0.5) {
                seq += (1 << i);
            }
        }

        pi.getEventInstance().setProperty("jail" + jailn, seq);
        mapProp = seq;
    }

    mapProp = Number(mapProp);
    if (mapProp != 0) {
        var countMiss = 0;
        for (var i = 1; i <= maxToggles; i++) {
            if (!(pi.getMap().getReactorByName("lever" + i).getState() == (mapProp >> i) % 2)) {
                countMiss++;
            }
        }

        const PacketCreator = Java.type('org.gms.util.PacketCreator');
        if (countMiss > 0) {
            map.broadcastMessage(PacketCreator.showEffect("quest/party/wrong_kor"));
            map.broadcastMessage(PacketCreator.playSound("Party1/Failed"));

            pi.playerMessage(5, "需要通过正确的拉杆组合才能继续前进。当前有 " + countMiss + " 个拉杆位置错误。");
            return false;
        }

        map.broadcastMessage(PacketCreator.showEffect("quest/party/clear"));
        map.broadcastMessage(PacketCreator.playSound("Party1/Clear"));
        pi.getEventInstance().setProperty("jail" + jailn, "0");
    }

    pi.playPortalSound();
    pi.warp(pi.getMapId() + 2, 0);
    return true;
}

function enterNoMobs(pi) {
    var map = pi.getMap();
    var mobcount = map.countMonster(9300044);

    if (mobcount > 0) {
        pi.playerMessage(5, "请先使用控制杆清除所有威胁再继续前进");
        return false;
    } else {
        pi.playPortalSound();
        pi.warp(pi.getMapId() + 2, 0);
        return true;
    }
}

function enter(pi) {
    var ret;
    if (leverSequenceExit) {
        ret = enterLeverSequence(pi);
    } else {
        ret = enterNoMobs(pi);
    }

    return ret;
}