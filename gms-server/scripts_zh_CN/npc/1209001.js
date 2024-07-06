var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("“#r黑魔法师#k的军队正在以不可阻挡的速度接近这里……我们别无选择，只能立刻逃离这个地区，留下我们的家园。哦，这是多么悲惨的事情！”");
    cm.dispose();
}