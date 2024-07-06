/*
	NPC Name: 		Insiginificant Being
	Map(s): 		Dungeon : Another Entrance
	Description: 		Takes you to another Dimension
*/

function start() {
    if (cm.getQuestStatus(6107) == 1 || cm.getQuestStatus(6108) == 1) {
        var ret = checkJob();
        if (ret == -1) {
            cm.sendOk("请组建一个队伍，然后再和我交谈。");
        } else if (ret == 0) {
            cm.sendOk("请确保你的队伍人数是2人。");
        } else if (ret == 1) {
            cm.sendOk("你的团队成员之一的职业不符合进入另一个世界的资格。");
        } else if (ret == 2) {
            cm.sendOk("你的队伍成员之一的等级不符合进入另一个世界的条件。");
        } else {
            var em = cm.getEventManager("s4aWorld");
            if (em == null) {
                cm.sendOk("由于未知原因，您不被允许进入。请再试一次。");
            } else if (em.getProperty("started") === "true") {
                cm.sendOk("另外一个世界已经有其他人在尝试击败小巴尔洛格了。");
            } else {
                var eli = em.getEligibleParty(cm.getParty());
                if (eli.size() > 0) {
                    if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                        cm.sendOk("“以你的名义注册的派对已经在此实例中注册。”");
                    }
                } else {
                    cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合尝试条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                }
            }
        }
    } else {
        cm.sendOk("你不被允许以未知的原因进入另一个世界。");
    }

    cm.dispose();
}

function action(mode, type, selection) {
}

function checkJob() {
    var party = cm.getParty();

    if (party == null) {
        return -1;
    }
    //    if (party.getMembers().size() != 2) {
    //	return 0;
    //    }
    var it = party.getMembers().iterator();

    while (it.hasNext()) {
        var cPlayer = it.next();

        if (cPlayer.getJobId() == 312 || cPlayer.getJobId() == 322 || cPlayer.getJobId() == 900) {
            if (cPlayer.getLevel() < 120) {
                return 2;
            }
        } else {
            return 1;
        }
    }
    return 3;
}