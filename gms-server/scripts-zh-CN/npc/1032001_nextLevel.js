/**
 * @description 魔法师教官汉斯，用nextLevel框架实现示例
*/

var job = 210;

const SPAWN_PNPC_FEE = 7000000;
const JOB_TYPE = 2;
const GameConstants = Java.type('org.gms.constants.game.GameConstants');

function start() {
    levelStart();
}

/**
 * @description 脚本开始执行入口
 */
function levelStart() {
    // 是魔法师职业，且能加入名人堂
    if (parseInt(cm.getJobId() / 100) === JOB_TYPE && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        levelStartHallOfFame();
    } else {
        levelStartChangeJob();
    }
}

/**
 * @description 处理名人堂相关的起始方法
 */
function levelStartHallOfFame() {
    let sendStr = "你已经走了很长的路才获得你今天所拥有的力量、智慧和勇气，不是吗？你觉得在#r冒险岛荣耀大厅，持有你当前角色的图像#k怎么样？";
    if (SPAWN_PNPC_FEE > 0) {
        sendStr += "只需要支付 #b " + cm.numberWithCommas(SPAWN_PNPC_FEE) + " 金币#k，我就可以帮你实现~";
    }
    // 选择否就调用levelDispose，选择是就走levelCheckHallOfFame
    cm.sendYesNoLevel("Dispose", "CheckHallOfFame", sendStr);
}

/**
 * @description 校验并执行名人堂操作
 */
function levelCheckHallOfFame() {
    if (cm.getMeso() < SPAWN_PNPC_FEE) {
        // 点击ok调用dispose
        cm.sendOkLevel("Dispose", "抱歉，您没有足够的金币购买在名人堂上的位置。");
        return;
    }

    const PlayerNPC = Java.type('org.gms.server.life.PlayerNPC');
    let msg;
    if (PlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
        cm.gainMeso(-SPAWN_PNPC_FEE);
        msg = "给你了！希望你会喜欢它。";
    } else {
        msg = "抱歉，名人堂目前已满...";
    }
    // 点击ok调用levelDispose
    cm.sendOkLevel("Dispose", msg);
}

/**
 * @description 处理转职相关的起始方法
 */
function levelStartChangeJob() {
    if (cm.getJobId() === 0) {
        // 1转，点击下一步进入levelStartFistJob
        cm.sendNextLevel("StartFistJob", "想成为一个#r魔法师#k吗？有一些标准需要满足，因为我们不能接受每个人... #b你的等级至少应该是8#k，首要目标是获得" + cm.getFirstJobStatRequirement(JOB_TYPE) + "。让我们看看。");   // thanks Vcoc for noticing a need to state and check requirements on first job adv starting message
    } else if (cm.getLevel() >= 30 && cm.getJobId() === 200) {
        // 2转
        if (cm.haveItem(4031012)) {
            // 点击下一步进入levelStartSecondJob1
            cm.sendNextLevel("StartSecondJob1", "我看到你做得很好。我会允许你迈出漫长道路上的下一步。");
        } else if (cm.haveItem(4031009)) {
            // 点击ok调用levelDispose
            cm.sendOkLevel("Dispose", "去找#b#p1072001##k。");
        } else {
            // 点击下一步进入levelStartSecondJob2
            cm.sendNextLevel("StartSecondJob2", "你取得的进步令人惊讶。");
        }
    } else if ((cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 === 0 && parseInt(cm.getJobId() / 100) === 2 && !cm.getPlayer().gotPartyQuestItem("JBP"))) {
        // 3转，点击下一步进入levelStartThirdJob1
        cm.sendNextLevel("StartThirdJob1", "你来了。几天前，奥西里亚的#b#p2020009##k跟我谈到了你。我看到你对成为魔法师的第三次职业转职感兴趣。为了实现这个目标，我需要测试你的实力，看看你是否配得上这个晋升。在金银岛的一个邪恶深林中有一个开口，会带你通往一个秘密通道。一旦进入，你将面对我的一个克隆。你的任务是打败他，并带着#b#t4031059##k回来。");
    } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
        // 3转，点击ok调用levelDispose
        cm.sendOkLevel("Dispose", "请给我带来#b#t4031059##k，它是从我的克隆体那里得到的。你可以在邪恶森林深处的空间洞穴里找到他。");
    } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
        // 3转，点击下一步进入levelStartThirdJob2
        cm.sendNextLevel("StartThirdJob2", "干得好。你打败了我的分身，并安全地带回了#b#t4031059##k。你现在已经从物理角度证明了自己配得上进行第三次职业转职。现在你应该把这条项链交给在奥西里亚的#b#p2020011##k，以进行测试的第二部分。祝你好运。你会需要的。");
    } else {
        // 点击ok调用levelDispose
        cm.sendOkLevel("Dispose", "明智的选择");
    }
}

/**
 * @description 1转处理入口
 */
function levelStartFistJob() {
    if (cm.getLevel() >= 8 && cm.canGetFirstJob(JOB_TYPE)) {
        // 点击否调用levelDispose，点击是走levelFinishFirstJob1
        cm.sendYesNoLevel("Dispose", "FinishFirstJob1", "哦...！你看起来就像是我们团队的一员... 你只需要一点邪恶的心思，然后... 是的... 那么，你觉得怎么样？想成为魔法师吗？");
    } else {
        // 点击ok调用levelDispose
        cm.sendOkLevel("Dispose", "再多训练一会儿，直到你达到基本要求，我就可以教你成为#r魔法师#k的方法。");
    }
}

function levelFinishFirstJob1() {
    if (cm.canHold(1372043)) {
        if (cm.getJobId() === 0) {
            cm.changeJobById(200);
            cm.gainItem(1372043, 1);
            cm.resetStats();
        }
        // 点击下一步进入levelFinishFirstJob2
        cm.sendNextLevel("FinishFirstJob2", "好的，从现在开始，你就是我们的一员了！你将在...过着流浪者的生活，但要耐心等待，很快你就会过上富裕的生活。好了，虽然不多，但我会传授给你一些我的能力... 哈啊啊啊！！");
    } else {
        // 点击ok调用levelDispose
        cm.sendOkLevel("Dispose", "清理一下你的背包，然后回来找我说话。");
    }
}

function levelFinishFirstJob2() {
    // 点击上一步返回levelFinishFirstJob1，下一步进入levelFinishFirstJob3
    cm.sendLastNextLevel("FinishFirstJob1", "FinishFirstJob3", "你现在变得更强大了。而且你的所有物品栏都增加了槽位。确切地说，是一整行。去亲自看看吧。我刚给了你一点点#bSP#k。当你在屏幕左下角打开#b技能#k菜单时，可以使用SP学习技能。不过要注意一点：你不能一次性全部提升。在学习了一些技能之后，你还可以获得一些特定的技能。");
}

function levelFinishFirstJob3() {
    // 点击上一步返回levelFinishFirstJob2，下一步进入levelFinishFirstJob4
    cm.sendLastNextLevel("FinishFirstJob2", "FinishFirstJob4", "但是要记住，技能并不是一切。作为一个魔法师，你的属性应该支持你的技能。魔法师主要使用智力作为他们的主属性，幸运作为次要属性。如果提升属性很困难，就使用#b自动分配#k。");
}

function levelFinishFirstJob4() {
    // 点击上一步返回levelFinishFirstJob3，下一步进入levelFinishFirstJob5
    cm.sendLastNextLevel("FinishFirstJob3", "FinishFirstJob5", "现在，我要再警告你一句。如果你从现在开始在战斗中失败，你将失去一部分总经验值。要特别注意这一点，因为你的生命值比大多数人都要少。");
}

function levelFinishFirstJob5() {
    // 点击上一步返回levelFinishFirstJob4，下一步直接levelDispose
    cm.sendLastNextLevel("FinishFirstJob4", "Dispose", "这是我能教你的全部了。祝你在旅途中好运，年轻的魔法师。");
}

/**
 * @description 2转处理入口1
 */
function levelStartSecondJob1() {
    // 选择0进入levelSecondJobSelect0，选择1进入levelSecondJobSelect1，选择2进入levelSecondJobSelect2，选择3进入levelSecondJobSelect3
    cm.sendSelectLevel("SecondJobSelect", "好的，当你做出决定后，点击底部的【我会选择我的职业】。#b\r\n#L0#请解释一下成为火毒法师是什么意思。\r\n#L1#请解释一下成为冰雷法师是什么意思。\r\n#L2#请解释一下成为牧师是什么意思。\r\n#L3#我会选择我的职业！");
}

function levelSecondJobSelect0() {
    // 点击下一步返回levelStartSecondJob1
    cm.sendNextLevel("StartSecondJob1", "掌握#r火/毒系魔法#k的魔法师。\r\n\r\n#b火/毒法师#k是一个活跃的职业，能够造成魔法元素伤害。这些技能使他们在面对对其元素弱点的敌人时具有显著优势。通过他们的技能#r冥想#k和#r减速#k，#b火/毒法师#k可以增加他们的魔法攻击并减少对手的移动能力。#b火/毒法师#k拥有强大的火焰箭攻击和毒素攻击。");    //f/p mage
}

function levelSecondJobSelect1() {
    // 点击下一步返回levelStartSecondJob1
    cm.sendNextLevel("StartSecondJob1", "掌握#r冰/雷系魔法#k的魔法师。\r\n\r\n#b冰/雷法师#k是一个活跃的职业，能够造成魔法元素伤害。这些技能使他们在面对对其元素弱点的敌人时具有显著优势。通过他们的技能#r冥想#k和#r减速#k，#b冰/雷法师#k可以增加他们的魔法攻击力并减少对手的移动能力。#b冰/雷法师#k拥有冰冻攻击和闪电攻击。");    //i/l mage
}

function levelSecondJobSelect2() {
    // 点击下一步返回levelStartSecondJob1
    cm.sendNextLevel("StartSecondJob1", "掌握#r神圣魔法#k的魔法师。\r\n\r\n#b牧师#k是一个强大的支持职业，必定会被任何队伍接受。这是因为他们有能力#r治疗#k自己和队伍中的其他成员。使用#r祝福#k，#b牧师#k可以增强属性并减少所受的伤害。如果你发现生存困难，这个职业是值得一试的。#b牧师#k对不死怪物尤其有效。");    //cleric
}

function levelSecondJobSelect3() {
    // 选择4进入levelSecondJobSelect4，选择5进入levelSecondJobSelect5，选择6进入levelSecondJobSelect6
    cm.sendSelectLevel("SecondJobSelect", "现在... 你做好决定了吗？请选择你想要在二转时选择的职业。#b\r\n#L4#法师（火 / 毒）\r\n#L5#法师（冰 / 闪电）\r\n#L6#牧师");
}

function levelSecondJobSelect4() {
    job = 210;
    // 选择否直接levelDispose，选择是进入levelFinishSecondJob1
    cm.sendYesNoLevel("Dispose", "FinishSecondJob1", "所以你想要选择第二次职业转职成为火/毒法师？你知道一旦在这里做出选择，就无法在第二次职业转职时选择不同的职业，对吧？");
}

function levelSecondJobSelect5() {
    job = 220;
    // 选择否直接levelDispose，选择是进入levelFinishSecondJob1
    cm.sendYesNoLevel("Dispose", "FinishSecondJob1", "所以你想要选择第二次职业转职成为冰/雷法师？你知道一旦在这里做出选择，就无法在第二次职业转职时选择不同的职业，对吧？");
}

function levelSecondJobSelect6() {
    job = 230;
    // 选择否直接levelDispose，选择是进入levelFinishSecondJob1
    cm.sendYesNoLevel("Dispose", "FinishSecondJob1", "所以你想要选择第二次职业转职成为牧师？你知道一旦在这里做出选择，就无法在第二次职业转职时选择不同的职业，对吧？");
}

function levelFinishSecondJob1() {
    if (cm.haveItem(4031012)) {
        cm.gainItem(4031012, -1);
    }
    cm.completeQuest(100008);
    // 下一步进入levelFinishSecondJob2
    cm.sendNextLevel("FinishSecondJob2", "好的，从现在开始你就是#b" + getJobName() + "#k了。法师是聪明的一群人，拥有令人难以置信的魔法能力，能够轻松地穿透怪物的心灵和心理结构……请每天都训练自己，我会帮助你变得比你现在更强大。");
    if (cm.getJobId() !== job) {
        cm.changeJobById(job);
    }
}

function levelFinishSecondJob2() {
    // 上一步返回levelFinishSecondJob1，下一步进入levelFinishSecondJob3
    cm.sendLastNextLevel("FinishSecondJob1", "FinishSecondJob3", "我刚刚给了你一本书，上面列出了你作为一名" + getJobName() + "可以获得的技能清单。此外，你的杂项物品栏也扩展了一行。你的最大生命值和魔法值也增加了。去检查一下，看看吧。");
}

function levelFinishSecondJob3() {
    // 上一步返回levelFinishSecondJob2，下一步进入levelFinishSecondJob4
    cm.sendLastNextLevel("FinishSecondJob2", "FinishSecondJob4", "我也给了你一点#bSP#k。打开左下角的#b技能菜单#k。你可以提升新获得的二级技能。不过要注意，你不能一次性提升它们。有些技能只有在学会其他技能后才能使用。记得要记住这一点。");
}

function levelFinishSecondJob4() {
    // 上一步返回levelFinishSecondJob3，下一步直接levelDispose
    cm.sendLastNextLevel("FinishSecondJob3", "Dispose", getJobName() + "需要继续变强！但若将自身的力量发泄在弱者身上，这并不是正确的方法。将自己所拥有的力量用在正确的事情上，这是比变得更强更为重要的课题。好了！相信你不断自我修炼，过不久就会再与我相见的，我期待那天的到来。");
}

/**
 * @description 2转处理入口1
 */
function levelStartSecondJob2() {
    if (!cm.isQuestStarted(100006)) {
        cm.startQuest(100006);
    }
    // 下一步进入levelStartSecondJob3
    cm.sendNextLevel("StartSecondJob3", "做得好。你看起来很强壮，但我需要看看你是否真的足够强大来通过测试，这不是一个困难的测试，所以你会做得很好。拿着我的信先……确保你不要丢了它！");
}

function levelStartSecondJob3() {
    if (cm.canHold(4031009)) {
        if (!cm.haveItem(4031009)) {
            cm.gainItem(4031009, 1);
        }
        // 上一步返回levelStartSecondJob2，下一步直接levelDispose
        cm.sendLastNextLevel("StartSecondJob2", "Dispose", "请将这封信交给#b#p1072001##k，他在埃利涅附近的#b#m101020000##k。他正在代替我担任教练的工作。把信交给他，他会代替我测试你。祝你好运。");
    } else {
        // ok调用levelDispose
        cm.sendOkLevel("Dispose", "请在你的背包中腾出一些空间。");
    }
}

/**
 * @description 3转处理入口1
 */
function levelStartThirdJob1() {
    if (cm.getPlayer().gotPartyQuestItem("JB3")) {
        cm.getPlayer().removePartyQuestItem("JB3");
        cm.getPlayer().removePartyQuestItem("JB3");
        cm.getPlayer().setPartyQuestItemObtained("JBP");
    }
    // 点击上一步，返回levelStartChangeJob，下一步直接levelDispose
    cm.sendLastNextLevel("StartChangeJob", "Dispose", "因为他是我的克隆，所以你可以期待一场艰难的战斗。他使用了许多特殊的攻击技能，与你以往所见的完全不同，你的任务是成功地与他一对一地战斗。在秘密通道中有一个时间限制，所以你必须在规定时间内打败他。祝你好运，希望你带着#b#t4031059##k。");
}

/**
 * @description 3转处理入口2
 */
function levelStartThirdJob2() {
    cm.getPlayer().removePartyQuestItem("JBP");
    cm.gainItem(4031059, -1);
    cm.gainItem(4031057, 1);
    levelDispose();
}

/**
 * @description 执行dispose
 */
function levelDispose() {
    cm.dispose();
}

/**
 * @description 根据jobId获取job名
 *
 * @returns job名
 */
function getJobName() {
    return job === 210 ? "#b法师（火/毒）#k" : (job === 220 ? "#b法师（冰/雷）#k" : "#b牧师#k");
}