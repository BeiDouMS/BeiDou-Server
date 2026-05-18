/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
//Boss Kitty

var status;
var questions;
var answers;
var correctAnswer;
var questionNum;

function start() {
    status = -1;
    questions = ["火焰浣熊不会掉落以下哪些物品？", "哪个NPC负责将旅客从废弃都市运送到古代神社，然后再运回？", "古代神社出售的哪些物品可以提高你的攻击力？", "以下哪些装备不会被删除？", "以下哪些物品不存在？", "昭和村的蔬菜店老板叫什么名字？", "这些物品中有哪些是存在的？", "古代神社最强的boss叫什么？", "这些物品中，哪一个的类别或级别描述不匹配？", "这些面条中，哪一条不是古代神社里NPC卖的？", "这些NPC中，哪一个不站在昭和电影院前？"]
    answers = [["浣熊柴火", "实心号角", "红砖"], ["佩利", "导游妮妮", "波利"], ["章鱼串", "日本炒面", "天妇罗"], ["徽章", "衣服", "项链"], ["冷冻金枪鱼", "风扇", "苍蝇拍"], ["萨米", "卡米", "由美"], ["云狐牙齿", "幽灵花束", "云狐尾巴"], ["天狗", "蓝蘑菇王", "姬神"], ["木精灵枪-战士专用武器", "橡皮榔头-单手剑", "枫树杖-51级装备"], ["日式拉面(豚骨)", "日式拉面(海鲜)", "味噌拉面"], ["绘里香", "黑泽", "阿利博士"]];
    correctAnswer = [1, 1, 0, 1, 2, 2, 2, 0, 0, 2, 2];
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (cm.isQuestStarted(8012) && !cm.haveItem(4031064)) { //quest in progress
                cm.sendYesNo("你都找到了吗？你打算尝试回答我所有的问题吗？");
            } else { //quest not started or already completed
                //cm.sendOk("喵喵喵！");//lol what's this?
                cm.dispose();
            }
        } else if (status == 1 && mode == 1) {
            var hasChicken = true;
            if (!cm.haveItem(2020001, 300)) {
                hasChicken = false;
            }
            if (!hasChicken) {
                cm.sendOk("什么？不行！300！三百。不少。如果你想要更多，就给我，但我至少需要300。我们不是所有人都像你一样又大又饱满…");
                cm.dispose();
            } else {
                cm.gainItem(2020001, -300)
                cm.sendNext("干得好！现在等一下……嘿，看这里！我这里有些食物！自己拿吧。好了，现在是时候问你们一些问题了。我相信你们已经意识到了，但记住，如果你们答错了，一切都结束了。要么全赢，要么全输！");
            }
        } else if (status == 7 && mode == 1) { //2-6 are the questions
            if (selection != correctAnswer.pop()) {
                cm.sendNext("嗯……反正所有人都会犯错！如果你想再试一次，那就给我拿300只炸鸡来。")
                cm.dispose();
            } else {
                cm.sendNext("哎呀，你所有的问题都答对了。总的来说，我可能不喜欢人类，但我讨厌食言，所以，正如我所承诺的，这是橙色大理石。")
            }
        } else if (status == 8 && mode == 1) { //gain marble
            cm.gainItem(4031064, 1);
            cm.sendOk("我们的交易已经结束，非常感谢！你可以离开了！");
            cm.dispose();
        } else if (status >= 2 && status <= 6 && mode == 1) {//questions
            var cont = true;
            if (status > 2) {
                if (selection != correctAnswer.pop()) {
                    cm.sendNext("嗯……反正所有人都会犯错！如果你想再试一次，那就给我拿300只炸鸡来。")
                    cm.dispose();
                    cont = false;
                }
            }
            if (cont) {
                questionNum = Math.floor(Math.random() * questions.length);
                if (questionNum != (questions.length - 1)) {
                    var temp;
                    temp = questions[questionNum];
                    questions[questionNum] = questions[questions.length - 1];
                    questions[questions.length - 1] = temp;
                    temp = answers[questionNum];
                    answers[questionNum] = answers[questions.length - 1];
                    answers[questions.length - 1] = temp;
                    temp = correctAnswer[questionNum];
                    correctAnswer[questionNum] = correctAnswer[questions.length - 1];
                    correctAnswer[questions.length - 1] = temp;
                }
                var question = questions.pop();
                var answer = answers.pop();
                var prompt = "问题 no." + (status - 1) + ": " + question;
                for (var i = 0; i < answer.length; i++) {
                    prompt += "\r\n#b#L" + i + "#" + answer[i] + "#l#k";
                }
                cm.sendSimple(prompt);
            }
        }
    }
}