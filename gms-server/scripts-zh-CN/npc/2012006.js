var status = -1;
var sel;

var destinations = ["魔法密林","玩具城","神木村","武陵","阿里安特","圣地"];
var boatType = ["飞船", "飞船", "飞艇", "鹤", "精灵", "渡船"];

function start() {
    var message = "天空之城的站台有许多个月台，请按照你的目的地进行选择。你想要前往哪里？\r\n";
    for (var i = 0; i < destinations.length; i++) {
        message += "\r\n#L" + i + "##b前往" + destinations[i] + "的" + boatType[i] + "站台.#l";
    }
    cm.sendSimple(message);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }
    status++;
    if (status == 0) {
        sel = selection;
        cm.sendNext("好的，我会把你送到 #b#m" + (200000110 + (sel * 10)) + "##k 的月台上。");
    } else if (status == 1) {
        cm.warp(200000110 + (sel * 10), "west00");
        cm.dispose();
    }
}