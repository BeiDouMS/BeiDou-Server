var status = -1;
var sel;

var destinations = ["魔法密林","玩具城","神木村","武陵","阿里安特","圣地"];
var boatType = ["the ship", "the train", "the bird", "Hak", "Genie", "the ship"];

function start() {
    var message = "该站有很多地方可供选择。你需要选择一个你想到达的地方。你会去那个地方？\r\n";
    for (var i = 0; i < destinations.length; i++) {
        message += "\r\n#L" + i + "##b前往" + destinations[i] + "的月台.#l";
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
        cm.sendNext("好的，我会把你送到 #b#m" + (200000110 + (sel * 10)) + "##k 的平台上。");
    } else if (status == 1) {
        cm.warp(200000110 + (sel * 10), "west00");
        cm.dispose();
    }
}