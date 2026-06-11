var status = -1;

var minLevel = 10;

var info = ["这里是什么地方？", "福斯魏德教授是谁？", "福斯魏德之门是什么？", "机械齿轮区在哪里？", "克拉奇亚丛林是什么地方？", "齿轮传送门是什么？", "街上的指示牌是什么意思？", "蒙面杰克是什么来头？", "莉塔·罗莉丝看起来很强，她有什么故事？", "城市的新区域什么时候开放？", "我要参加新叶城问答！"];

var answers = [

    "新叶城是一座欢迎所有冒险家的城市。这里还在成长，也有很多朋友正在帮忙建设。你可以把这里当作新的据点，慢慢熟悉每一条街道。",

    "福斯魏德教授是一位天才发明家，也是一名奇妙的时空旅行者。他帮助我们研究传送技术，并在城市里留下了不少让人惊讶的装置。",

    "福斯魏德之门是一种特殊传送门。站到门前按上方向键，就能被送到对应地点。熟悉这些传送门，会让你在新叶城行动得更方便。",

    "机械齿轮区位于大本钟塔的下方，那里充满了古怪的机械和危险的怪物。如果你准备去调查，请务必带好补给。",

    "克拉奇亚丛林位于新叶城郊外。那里生态奇特，怪物也更凶猛，想深入探索的话，最好先做好战斗准备。",

    "齿轮传送门是古老机械技术的一部分，能把人送到齿轮结构中的其他位置。它不像普通传送门那样总是成对出现，所以要多留意出口。",

    "街上的施工指示牌代表城市开发进度。红色通常表示暂未开放，绿色表示可以通行。新叶城一直在扩建，记得常回来看看。",

    "蒙面杰克是个神秘人物，总喜欢把真实身份藏起来。他来自阿莫利亚，嘴上不饶人，但并不是坏人。想知道更多，也许你可以亲自去问他。",

    "莉塔·罗莉丝是值得信赖的伙伴。她接受过盗贼训练，身手非常敏捷，也愿意保护这座城市。如果你想为市民出一份力，可以去找她聊聊。",

    "新的城区会在准备完成后开放。开发者们正在努力建设，等安全检查通过，我们就会让冒险家进入。",

    "没问题。如果你等级达到要求，我会让你开始新叶城问答。答对问题后，会有不错的奖励。"

];

var selected = -1;



function start() {

    action(1, 0, 0);

}



function action(mode, type, selection) {

    if (mode == -1 || (status >= 0 && mode == 0 && type > 0)) {

        cm.dispose();

        return;

    }

    if (mode == 1) {

        status++;

    } else {

        status--;

    }



    if (status == 0) {

        if (cm.isQuestCompleted(4911)) {

            cm.sendOk("干得好！你已经解答了我关于新叶城的所有问题。祝你旅途愉快！");

            cm.dispose();

            return;

        }

        if (cm.isQuestCompleted(4900) || cm.isQuestStarted(4900)) {

            cm.sendOk("嘿，注意一下，我还要考你另一个问题。准备好了就继续完成问答吧！");

            cm.dispose();

            return;

        }

        var text = "你好！我是#b#p9201050##k，新叶城市长。欢迎来到新叶城！你想了解什么？#b";

        for (var i = 0; i < info.length; i++) {

            text += "\r\n#L" + i + "#" + info[i] + "#l";

        }

        cm.sendSimple(text);

    } else if (status == 1) {

        selected = selection;

        if (selected == 10) {

            if (cm.getLevel() >= minLevel) {

                cm.sendNext("没问题。如果你回答正确，我会给你一些好东西。准备开始吧！");

                cm.startQuest(4900);

            } else {

                cm.sendNext("别急。等你达到#b" + minLevel + "级#k以后，再来参加新叶城问答吧。先在城市里多探索一下。 ");

            }

            cm.dispose();

        } else if (selected >= 0 && selected < answers.length) {

            cm.sendNext(answers[selected]);

        } else {

            cm.dispose();

        }

    } else if (status == 2) {

        status = -1;

        action(1, 0, 0);

    }

}

