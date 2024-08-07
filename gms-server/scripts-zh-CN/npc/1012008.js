/*
 * @Author - Sparrow
 * @NPC - 1012008 - Casey the Game Master
 * @Map - 100000203 - Henesys Game Park
 */

var status;
var current;
var omok = [4080000, 4080001, 4080002, 4080003, 4080004, 4080005];
var omok1piece = [4030000, 4030000, 4030000, 4030010, 4030011, 4030011];
var omok2piece = [4030001, 4030010, 4030011, 4030001, 4030010, 4030001];
var omokamount = 99; //制作五子棋需要的五目石数量
var text = "棋具的选择也取决于你想要使用哪种棋子。你想制作哪种棋具呢？"

function start() {
    current = 0;
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 && current > 0) {
        cm.dispose();
        return;
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
    }

    if (status == 0) {
        cm.sendSimple("嘿，看起来你需要休息一下，远离那些狩猎。你应该像我一样享受生活。嗯，如果你有一些物品，我可以和你交换一个可以玩迷你游戏的物品。现在...我能为你做些什么？#b\r\n#L0#制作一个迷你游戏物品#l\r\n#L1#向我解释一下迷你游戏是什么#l#k");

    } else if (status == 1) {
        if (selection == 0) {
            cm.sendSimple("你想制作迷你游戏道具吗？迷你游戏并不是你随便就能玩的。每个迷你游戏都需要一组特定的道具。你想制作哪个迷你游戏的道具？#b\r\n#L4#五子棋套装#l\r\n#L5#配对卡套装#l#k");
        } else if (selection == 1) {
            cm.sendSimple("你想了解更多关于小游戏吗？太棒了！问我任何事情。你想了解哪个小游戏？#b\r\n#L2#五子棋#l\r\n#L3#配对卡片#l#k");
        }

    } else if (status == 2) {
        if (selection == 2) {
            current = 1;
            cm.sendNext("这里是Omok的规则，请仔细听。Omok是一种游戏，你和对手轮流在桌子上放置棋子，直到有人找到一种方法在一条线上放置5个连续的棋子，无论是水平、对角线还是垂直线。首先，只有拥有#bOmok Set#k的人才能开设游戏房间。");
        } else if (selection == 3) {
            current = 2;
            cm.sendNext("这里是Match Cards的规则，请仔细听。顾名思义，Match Cards就是在桌子上找到一对匹配的牌。当所有匹配的对都被找到时，拥有更多匹配对的人将赢得游戏。就像五子棋一样，你需要一副Match Cards牌来开启游戏房间。");

        } else if (selection == 4) {
            current = 3;
            cm.sendNext("你想玩#b五子棋#k，是吧？要玩这个游戏，你需要五子棋套装。只有拥有这个物品的人才能开设五子棋游戏房间，而且你几乎可以在市场以外的任何地方玩这个游戏。");

        } else if (selection == 5) {
            current = 4;
            if (cm.haveItem(4030012, 15)) {
                cm.gainItem(4030012, -15);
                cm.gainItem(4080100, 1);
                cm.dispose();
            } else {
                cm.sendNext("你想要 #b一套配对卡#k 吗？嗯...要制作一套配对卡，你需要一些 #b怪物卡#k。怪物卡可以通过在整个岛上打倒怪物来获得。收集15张怪物卡，你就可以制作一套配对卡。"); //Lmfao a set of A set xD
                cm.dispose();
            }
        }


    } else if (status == 3) {
        if (current == 1) {
            cm.sendNextPrev("每局五子棋游戏将花费你#r100金币#k。即使你没有#b五子棋套装#k，你也可以进入房间并进行游戏。然而，如果你没有100金币，那么你将根本无法进入房间。开设游戏房间的人也需要100金币来开房间（否则就没有游戏）。如果在游戏过程中你的金币用完了，那么你将被自动踢出房间！");
        } else if (current == 2) {
            cm.sendNextPrev("每一局配对卡游戏都需要花费你 #r100金币#k。即使你没有 #b一套配对卡#k，你也可以进入房间并进行游戏。然而，如果你没有100金币，那么你将根本无法进入房间。开设游戏房间的人也需要100金币来开房间（否则就没有游戏）。如果你在游戏过程中金币用完了，那么你将被自动踢出房间！");

        } else if (current == 3) {
            for (var i = 0; i < omok.length; i++) {
                text += "\r\n#L" + i + "##b#t" + omok[i] + "##k#l";
            }
            cm.sendSimple(text);
        }

    } else if (status == 4) {
        if (current == 1 || current == 2) {
            cm.sendNextPrev("进入房间，当你准备好玩的时候，点击#b准备#k。\r\n一旦访客点击#b准备#k，房间所有者可以按#b开始#k开始游戏。如果一个不受欢迎的访客走进来，而你不想和那个人玩，房间所有者有权将访客踢出房间。在那个人右边会有一个带有x的方框。点击那个方框说一声冷冷的再见，好吗？"); //Oh yeah, because people WALK in Omok Rooms.
        } else if (current == 3) {
            if (cm.haveItem(omok1piece[selection], omokamount) && cm.haveItem(omok2piece[selection], omokamount) && cm.haveItem(4030009, 1)) {
                cm.gainItem(omok1piece[selection], -omokamount);
                cm.gainItem(omok2piece[selection], -omokamount);
                cm.gainItem(4030009, -1);
                cm.gainItem(omok[selection], 1);
                cm.dispose();
            } else {
                cm.sendNext("#b你准备好 #t" + omok[selection] + "##k了嘛? 嗯...给我材料，我就可以那么做。仔细听，你需要的材料将会是： #r" + omokamount + " #t" + omok1piece[selection] + "#, " + omokamount + " #t" + omok2piece[selection] + "#, 1 #t" + 4030009 + "##k. 怪物们可能会时不时地掉落那些东西……");

                cm.dispose();
            }
        }

    } else if (status == 5) {
        if (current == 1) {
            cm.sendNextPrev("当第一局开始时，#b房主先走#k。请注意，你会被给予一个时间限制，如果你没有在规定时间内行动，你可能会失去你的回合。通常情况下，3 x 3 是不允许的，但如果有一个必须把你的棋子放在那里或者面临游戏结束的时刻，那么你可以把它放在那里。3 x 3 是允许作为最后的防线！哦，如果是#r6或7连#k，那就不算。只有5连！");
        } else if (current == 2) {
            cm.sendNextPrev("哦，与Omok不同的是，当你创建配对卡游戏房间时，你需要设置游戏使用的卡片数量。有3种可用模式，3x4，4x5和5x6，分别需要12、20和30张卡片。请记住，一旦房间开启，你将无法更改设置，所以如果你真的想更改设置，可能需要关闭房间并重新开启另一个。");
        }

    } else if (status == 6) {
        if (current == 1) {
            cm.sendNextPrev("如果你知道自己陷入了困境，你可以请求一次#b重做#k。如果对手接受了你的请求，那么你和对手的最后一步将会被取消。如果你需要上厕所，或者需要休息一段时间，你可以请求一次#b平局#k。如果对手接受了请求，游戏将以平局结束。提示：这可能是保持友谊的好方法。");
        } else if (current == 2) {
            cm.sendNextPrev("当第一局开始时，#b房间所有者先行。#k请注意，你将被给予一个时间限制，如果你没有在规定时间内行动，你可能会失去你的回合。当你在你的回合上找到一对匹配的牌时，只要你继续找到一对匹配的牌，你就可以保持你的回合。利用你的记忆技巧来连续翻牌。");
        }

    } else if (status == 7) {
        if (current == 1) {
            cm.sendPrev("当下一局游戏开始时，输家将先行。同时，不允许任何人在游戏进行中离开。如果你这么做了，你可能需要请求#b认输或者平局#k。（当然，如果你请求认输，你就会输掉游戏。）而且，如果你在游戏进行中点击“离开”，并且在游戏结束后要求离开，你将在游戏结束后立即离开房间。这将是一种更加有用的离开方式。");
            cm.dispose();
        } else if (current == 2) {
            cm.sendNextPrev("如果你和对手拥有相同数量的匹配对，那么拥有更长匹配对连续的人将获胜。如果你需要上厕所或者休息一段时间，你可以请求一个#btie#k。如果对手接受请求，游戏将以平局结束。提示：这可能是保持友谊的好方法。");
        }
    } else if (status == 8) {
        if (current == 2) {
            cm.sendPrev("当下一局游戏开始时，输家将先行。同时，不允许任何人在游戏进行中离开。如果你这么做了，你可能需要请求#b认输或者平局#k。（当然，如果你请求认输，你就会输掉游戏。）而且，如果你在游戏进行中点击“离开”，并且在游戏结束后要求离开，你将在游戏结束后立即离开房间。这将是一种更加有用的离开方式。");
            cm.dispose();
        }
    }
}  