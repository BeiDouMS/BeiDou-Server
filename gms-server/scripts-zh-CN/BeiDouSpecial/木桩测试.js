/**
 * 稻草人副本入口 NPC
 * 基于 WuGongPQ 入口脚本改写
 * Boss: 9001007 稻草人，血量 21 亿
 */
const isRepeat = true;               // 允许完成任务后重复进入
const EventName = 'DummyTest';      // 事件名称（请与事件脚本文件名保持一致）
const EventLevel = 1;                // 难度级别（固定为 1，不再影响血量）
const LevelMin = 1, LevelMax = 255;  // 等级限制
var em = null;

function start() {
    if (em == null) {
        em = cm.getEventManager(EventName);
    }
    if (em == null || em.getName() !== EventName) {
        cm.sendOkLevel('', '由于某种神秘力量，暂时无法进入副本，请联系管理员。');
        cm.dispose();
        return;
    }
    cm.sendSelectLevel('', `敬礼！\r\n你好，#e#b#h ##k#n，我是#b#e#p${cm.getNpc()}##k#n，想做点什么呢？\r\n\r\n#L0##b挑战稻草人#l\r\n#L1#离开#l#k`);
}

function level() {
    cm.dispose();
}
function levelnull() {
    cm.dispose();
}
function leveldispose() {
    cm.dispose();
}

function level0() {
    if (cm.getPlayer().getMapId() !== 910000000) {
        cm.sendOkLevel('', '无论下定了怎样的决心，都必须要做好充分的准备工作，请先前往#r#e自由市场#k#n！');
        cm.dispose();
        return;
    }
    cm.sendNextLevel('0_1', '你竟然想要挑战传说中的稻草人……\r\n它拥有惊人的#r#e 21 亿血量#k#n，你准备好了吗？');
}

function level0_1() {
    if (cm.getParty() == null) {
        cm.sendOkLevel('', '挑战稻草人非常危险，请至少组建一个队伍（即使是单人队伍）再来找我。');
        cm.dispose();
    } else if (!cm.isLeader()) {
        cm.sendOkLevel('', '请让你的队长来开始这个任务。');
        cm.dispose();
    } else {
        var eli = em.getEligibleParty(cm.getParty());
        if (eli.size() > 0) {
            if (em.startInstance(cm.getParty(), cm.getPlayer().getMap(), EventLevel)) {
                // 启动成功
            } else {
                cm.sendOk('已经有其他队伍正在挑战稻草人，请稍后再试。');
            }
        } else {
            cm.sendOk('你的队伍不符合要求：' + em.getProperty('party'));
        }
        cm.dispose();
    }
}

function level1() {
    cm.dispose();
}