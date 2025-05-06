var fid = "glpq_s";

function action() {
    rm.mapMessage(6, "所有吸血蝙蝠已消失！");
    rm.getMap().killAllMonsters(true);
    eim.setIntProperty(fid, 777);
}

function touch() {
    var eim = rm.getEventInstance();

    if (eim.getIntProperty(fid) == 5) {
        action();
    }
    eim.setIntProperty(fid, eim.getIntProperty(fid) + 1);
}

function untouch() {
    var eim = rm.getEventInstance();

    if (eim.getIntProperty(fid) == 5) {
        action();
    }
    eim.setIntProperty(fid, eim.getIntProperty(fid) - 1);
}