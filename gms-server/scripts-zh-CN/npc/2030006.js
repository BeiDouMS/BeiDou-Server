/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
/* Holy Stone
	Holy Ground at the Snowfield (211040401)
	3rd job advancement - Question trial.
 */

var questionTree = [
        //Questions Related to CHARACTERS
        ["在冒险岛中，从Lv1升到Lv2需要多少经验值？", ["20", "15", "4", "12", "16"], 1],
        ["各职业一转时，下列哪项要求是错误的？", ["魔法师 - 等级8级", "海盗 - 敏捷不低于20", "弓箭手 - 敏捷不低于25", "飞侠 - 敏捷不低于20", "战士 - 力量不低于35"], 3],
        ["被怪物攻击时特别的异常状态没有被正确说明的是哪一项？", ["封印 - 不能释放技能", "不死族 - 变成不死族 & 恢复效果减半", "虚弱-移动速度降低", "诅咒 - 经验减少", "昏迷 - 无法移动"], 2],
        ["各职业一转时，下列哪项要求是正确的？", ["海盗 - 25幸运", "魔法师 - 10级", "飞侠 - 25 幸运", "战士 - 30力量", "弓箭手 - 25敏捷"], 4],

        //Questions Related to ITEMS
        ["下列怪物中，哪组怪物与打倒他所能得到的战利品是正确的对应关系？", ["仙人掌宝宝 - 刺针", "钢甲猪 - 野猪牙", "红小丑 - 黄小丑的帽子", "松松 - 坚果", "蝙蝠 - 蝙蝠翅膀"], 4],
        ["下列怪物中，哪组怪物与打倒他所能得到的战利品是错误的对应关系", ["大侠 - 大侠勋章", "食人花 - 食人花的叶子", "古木妖 - 苗木", "小海象 - 海象尖牙", "僵尸 - 僵尸丢失的臼齿"], 1],
        //["In GM Event, how many FRUIT CAKE you can get as reward?", ["20", "200", "5", "25", "100"], 2],
        ["下列药品中，哪组药品与功能是正确的对应关系？", ["活力神水 - 攻击 +5 持续 3 分钟", "纯净水 - 回复 700 MP", "蛋糕 - 回复 150 HP & MP", "沙拉 - 回复 300 MP", "披萨饼 - 回复400 HP"], 4],
        ["下列药品中，哪组药品与功能是错误的对应关系？", ["活力神水 - 回复 300 MP", "补药 - 恢复虚弱状态", "苹果 - 回复 30 HP", "清晨之露 - 回复3000 MP", "拉面 - 回复 1000 HP"], 3],

        //Questions Related to MONSTERS
        ["绿蘑菇，木妖，蓝水灵，斧木妖，三眼章鱼中级别最高的怪物是哪一个？", ["三眼章鱼", "蓝水灵", "斧木妖", "木妖", "绿蘑菇"], 2],
        ["往返于魔法密林/天空之城的船上会出现哪个怪物？", ["狼人", "绿水灵", "蝙蝠魔", "扎昆", "皮克西"], 2],
        ["在彩虹岛没有出现的怪物是？", ["蘑菇仔", "蓝蜗牛", "绿水灵", "红蜗牛", "飘飘猪"], 4],    // to get conformant with website answers, thanks to Vcoc
        ["在金银岛没有出现的怪物是？", ["火独眼兽", "石球", "蝙蝠怪", "黑木妖", "绿蜗牛"], 1],
        ["在冰封雪域没有出现的怪物是？", ["黑雪人", "黑鳄鱼", "法老王企鹅", "火焰猎犬", "僵尸"], 1],
        ["以下哪种怪物会飞?", ["巫婆", "鳄鱼", "冰独眼兽", "猫鼬", "阿丽莎乐"], 0],
        ["在神秘岛没有出现的怪物是？", ["星光精灵", "幼黄独角狮", "幼红独角狮", "鳄鱼", "野狼"], 3],
        ["在彩虹岛没有出现的怪物是？", ["绿蜗牛", "蘑菇仔", "火独眼兽", "花蘑菇", "蓝水灵"], 2],

        //Questions Related to QUESTS
        ["唤醒麦吉不需要的材料是哪一个？", ["火焰羽毛", "旧战剑", "冰块", "星石", "妖精之翼"], 4],
        ["以下哪项任务是可以重复完成的?", ["医院之谜", "正义的捐赠", "幽灵的行踪", "艾温的玻璃鞋", "玛雅和奇怪的药物"], 3],
        ["以下哪项不是二转职业", ["巫师", "牧师", "刺客", "枪手", "勇士"], 0],
        ["以下哪项任务要求的等级最高？", ["丘比特信使", "迷失在海洋中", "阿尔卡斯特和黑暗水晶", "消灭兔子", "庞庞的战争"], 2],

        //Questions Related to TOWN/NPC
        ["金银岛没有的村落是？", ["金海滩", "彩虹村", "明珠港", "勇士部落", "魔法密林"], 1],
        ["你在彩虹岛遇到的第一个NPC是谁？", ["塞拉", "希娜", "路卡斯", "罗杰", "尚克斯"], 1],
        ["在冰封雪域看不到的NPC是？", ["伯坚", "索菲亚", "佩德罗", "珀斯上尉", "卢米"], 1],
        ["在冰封雪域看不到的NPC是？", ["魔法石", "格里巴", "杰夫", "神圣的石头", "保姆珥玛"], 4],
        ["在勇士部落看不到的NPC是？", ["伊安", "索菲亚", "斯密斯", "易德", "麦吉"], 3],
        ["在射手村看不到的NPC是？", ["特奥", "赫丽娜", "玛亚", "皮亚", "莉娜"], 0],
        ["在魔法密林看不到的NPC是？", ["汉斯", "易德", "露饵", "妖精路易", "赛恩"], 2],
        ["在废弃都市看不到的NPC是？", ["后街吉姆", "马龙", "休咪", "鲁克", "钱老板"], 3],
        ["哪个NPC与宠物无关?", ["科尔", "比休斯", "帕特里沙", "威巴", "科洛伊"], 1],
        ["废弃都市中，离家出走的少年阿列克斯的父亲是谁？", ["长老斯坦", "后街吉姆", "铭仁", "比休斯", "卢克"], 0],
        ["哪个NPC不属于天空之城阿尔法小队？", ["查理中士", "巴伯下士", "伊吉上等兵", "珀斯上尉", "彼特"], 4],
        ["在二转过程中，收集30个黑色珠子给转职教官后可以得到什么？", ["古老的戒指", "记忆粉", "仙尘", "英雄证书", "秘密卷轴"], 3],
        ["为了给射手村的玛雅治病，需要给她什么？", ["苹果", "强力灵药", "奇怪的药", "菊花", "橙汁"], 2],
        ["以下与合成或冶炼工作没有关系的NPC是？", ["奈巴", "塞利尔", "塞恩", "易德", "后街吉姆"], 2],
        ["在彩虹岛看不到的NPC是？", ["巴里", "特奥", "皮奥", "赛德", "玛利亚"], 1],
        ["在导航室的监视器里能看到谁和Kyrin在一起？", ["路卡斯", "金博士", "长老斯坦", "斯卡德", "弗利维教授"], 1],
        ["你知道射手村的赫丽娜吗？他的眼睛是什么颜色？", ["蓝色", "绿色", "棕色", "红色", "黑色"], 1],
        ["勇士部落武术教练的帽子上有多少根羽毛？", ["7", "8", "3", "13", "16"], 3],
        ["魔法密林汉斯持有的宝珠是什么颜色?", ["白色", "橙色", "蓝色", "紫色", "绿色"], 2]
    ];

var status;
var question;

var questionPool;
var questionPoolCursor;

var questionAnswer;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getPlayer().gotPartyQuestItem("JBQ") && !cm.haveItem(4031058, 1)) {
                if (cm.haveItem(4005004, 1)) {
                    if (!cm.canHold(4031058)) {
                        cm.sendNext("接受此试炼前，请确保有一个空闲的ETC槽位。");
                        cm.dispose();
                    } else {
                        cm.sendNext("好的...我将在这里测试你的智慧。回答所有问题正确，你就会通过测试，但是，如果你有一次说谎，那么你就得重新开始，好吗，我们开始吧。");
                    }
                } else {
                    cm.sendNext("给我带来一个 #b#t4005004##k 以继续进行试炼。");
                    cm.dispose();
                }
            } else {
                cm.dispose();
            }
        } else if (status == 1) {
            cm.gainItem(4005004, -1);
            instantiateQuestionPool();

            question = fetchNextQuestion();
            var questionHead = generateQuestionHeading();
            var questionEntry = questionTree[question][0];

            var questionData = generateSelectionMenu(questionTree[question][1], questionTree[question][2]);
            var questionOptions = questionData[0];
            questionAnswer = questionData[1];

            cm.sendSimple(questionHead + questionEntry + "\r\n\r\n#b" + questionOptions + "#k");
        } else if (status >= 2 && status <= 5) {
            if (!evaluateAnswer(selection)) {
                cm.sendNext("你已经失败了这个问题。");
                cm.dispose();
                return;
            }

            question = fetchNextQuestion();
            var questionHead = generateQuestionHeading();
            var questionEntry = questionTree[question][0];

            var questionData = generateSelectionMenu(questionTree[question][1], questionTree[question][2]);
            var questionOptions = questionData[0];
            questionAnswer = questionData[1];

            cm.sendSimple(questionHead + questionEntry + "\r\n\r\n#b" + questionOptions + "#k");
        } else if (status == 6) {
            if (!evaluateAnswer(selection)) {
                cm.sendNext("你已经失败了这个问题。");
                cm.dispose();
                return;
            }

            cm.sendOk("好的。你的所有答案都被证明是真实的。你的智慧得到了验证。拿着这条项链回去吧。");
            cm.gainItem(4031058, 1);
            cm.dispose();
        } else {
            cm.sendOk("意外的分支。");
            cm.dispose();
        }
    }
}

function evaluateAnswer(selection) {
    return selection == questionAnswer;
}

function generateQuestionHeading() {
    return "Here's the " + (status) + (status == 1 ? "st" : status == 2 ? "nd" : status == 3 ? "rd" : "th") + " question. ";
}

function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

function instantiateQuestionPool() {
    questionPool = [];

    for (var i = 0; i < questionTree.length; i++) {
        questionPool.push(i);
    }

    shuffleArray(questionPool);
    questionPoolCursor = 0;
}

function fetchNextQuestion() {
    var next = questionPool[questionPoolCursor];
    questionPoolCursor++;

    return next;
}

function shuffle(array) {
    var currentIndex = array.length, temporaryValue, randomIndex;

    // While there remain elements to shuffle...
    while (0 !== currentIndex) {

        // Pick a remaining element...
        randomIndex = Math.floor(Math.random() * currentIndex);
        currentIndex -= 1;

        // And swap it with the current element.
        temporaryValue = array[currentIndex];
        array[currentIndex] = array[randomIndex];
        array[randomIndex] = temporaryValue;
    }

    return array;
}

function generateSelectionMenu(array, answer) {
    var answerStr = array[answer], answerPos = -1;

    shuffle(array);

    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
        if (answerStr == array[i]) {
            answerPos = i;
        }
    }
    return [menu, answerPos];
}
