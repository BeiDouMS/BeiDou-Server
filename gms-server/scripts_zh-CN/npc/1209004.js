var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("我希望这次旅行能够安全无恙，希望我们能在那里生活在一个更加宁静的地方……嘿，亲爱的，让我们走吧。");
    cm.dispose();
}