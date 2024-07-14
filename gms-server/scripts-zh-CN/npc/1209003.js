var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("我们将暂时前往#b金银岛#k。我听说#r黑魔法师#k本人还不能夺取那个地方，多亏了#b被施加在那个地区的封印#k。我们为他们的安全祈祷，但如果命运不眷顾英雄们，至少我们一旦抵达大陆就会安全了。");
    cm.dispose();
}