function start() {
    cm.sendSimple("如果你有翅膀，我相信你可以去那里。但是，仅仅这样还不够。如果你想穿越比刀锋还锋利的风，你还需要坚硬的鳞片。我是唯一知道回去的半人半龙……如果你想去那里，我可以变你。无论你是什么，此刻，你将成为一只#b龙#k……\r\n#L0##b我想成为一只龙。#k#l");
}

function action(m, t, s) {
    if (m > 0) {
        cm.useItem(2210016);
        cm.warp(200090500, 0);
    }
    cm.dispose();
}  