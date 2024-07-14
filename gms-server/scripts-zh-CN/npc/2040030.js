/* Author: aaroncsn <MapleSea Like, Need to add creation of minigame>
	NPC Name: 		Wisp
	Map(s): 		Ludibrium: Eos Tower Entrance(220000400)
	Description: 		Pet Master
*/

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendSimple("你好，我是金银岛的主要门徒#b玛尔仙子#k。玛尔仙子召唤我来这里，看看在卢德里布里姆这里宠物们是否得到了照顾。我能为你做些什么呢？\r\n#L0##b我的宠物变成了玩偶\r\n请帮我让它再次活动起来！#k#l \r\n#L1##b告诉我更多关于宠物的事情。#k#l \r\n#L2##b我如何养宠物？#k#l \r\n#L3##b宠物也会死吗？#k#l \r\n#L4##b棕色和黑色小猫的指令是什么？#k#l \r\n#L5##b棕色小狗的指令是什么？#k#l \r\n#L6##b粉色和白色小兔的指令是什么？#k#l \r\n#L7##b小型货物的指令是什么？#k#l \r\n#L8##b哈士奇的指令是什么？#k#l \r\n#L9##b黑猪的指令是什么？#k#l \r\n#L10##b熊猫的指令是什么？#k#l \r\n#L11##b恐龙男孩和女孩的指令是什么？#k#l \r\n#L12##b鲁道夫的指令是什么？#k#l \r\n#L13##b猴子的指令是什么？#k#l \r\n#L14##b机器人的指令是什么？#k#l \r\n#L15##b大象的指令是什么？#k#l \r\n#L16##b金猪的指令是什么？#k#l \r\n#L17##b企鹅的指令是什么？#k#l \r\n#L18##b小雪人的指令是什么？#k#l \r\n#L19##b少年巴尔洛格的指令是什么？\r\n#L20##b幼龙的指令是什么？#k#l \r\n#L21##b绿色/红色/蓝色龙的指令是什么？#k#l \r\n#L22##b黑龙的指令是什么？#k#l \r\n#L23##b雪人的指令是什么？#k#l \r\n#L24##b孙悟空的指令是什么？#k#l \r\n#L25##b少年死神的指令是什么？#k#l \r\n#L26##b水晶鲁道夫的指令是什么？#k#l \r\n#L27##b基诺的指令是什么？#k#l \r\n#L28##b白鸭的指令是什么？#k#l \r\n#L29##b粉豆人的指令是什么？#k#l \r\n#L30##b箭猪的指令是什么？#k#l");
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendNext("我是维斯普，继续进行我的主人玛尔仙女交给我的研究。在鲁迪布里姆甚至有很多宠物。我需要继续我的研究，所以请原谅我…");
                cm.dispose();
            } else if (selection == 1) {
                cm.sendNext("嗯，你一定有很多关于宠物的问题。很久以前，一个名叫#bCloy#k的人，给它喷洒了生命之水，并施加了魔法，创造了一种神奇的动物。我知道这听起来难以置信，但它是一个变成真实生物的玩偶。它们非常理解并且能很好地跟随人类。");
            } else if (selection == 2) {
                cm.sendNext("根据您给出的指令，宠物可能会喜欢、讨厌，并对其显示其他种类的反应。如果您给宠物一个指令，它能够很好地跟随您，您们之间的亲密度会提高。双击宠物，您可以检查亲密度、等级、饱食度等等...");
            } else if (selection == 3) {
                cm.sendNext("垂死……嗯，严格来说它们并不算是活着的，所以我不确定“垂死”是否是正确的术语。它们是用我的魔法力量和生命之水变成活物的娃娃。当然，它们活着的时候就像活生生的动物一样……");
            } else if (selection == 4) {
                cm.sendNext("这些是#r棕色小猫和黑色小猫#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏猫, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 10 ~ 30)\r\n#b可爱#k (等级 10 ~ 30)\r\n#b站起来, 站, 起来#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 5) {
                cm.sendNext("这些是#r棕色小狗#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏狗, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 坏狗, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b撒尿#k (等级 10 ~ 30)\r\n#b说话, 说, 聊天, 叫#k (等级 10 ~ 30)\r\n#b趴下#k (等级 10 ~ 30)\r\n#b站起来, 站, 起来#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 6) {
                cm.sendNext("这些是#r粉红兔子和白兔子#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b站起来, 站#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 10 ~ 30)\r\n#b拥抱#k (等级 10 ~ 30)\r\n#b睡觉, 困了, 上床#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 7) {
                cm.sendNext("这些是#r迷你货物#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b站起来, 站立#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b撒尿#k (等级 1 ~ 30)\r\n#b说话, 聊天#k (等级 10 ~ 30)\r\n#b魅力, 魅力值#k (等级 10 ~ 30)\r\n#b乖孩子, 乖#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 8) {
                cm.sendNext("这些是#rHusky#k的指令。指令旁边提到的等级是宠物需要的等级才能回应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (等级 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (等级 1 ~ 30)\r\n#biloveyou#k (等级 1 ~ 30)\r\n#bpee#k (等级 1 ~ 30)\r\n#btalk, say, chat, bark#k (等级 10 ~ 30)\r\n#bdown#k (等级 10 ~ 30)\r\n#bup, stand, rise#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 9) {
                cm.sendNext("这些是#r黑猪#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏猪, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b举手, 站起#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天, 拥抱#k (等级 10 ~ 30)\r\n#b微笑#k (等级 10 ~ 30)\r\n#b笑, 微笑#k (等级 10 ~ 30)\r\n#b魅力, 睡觉, 困了, 上床#k(等级 20~30)");
                cm.dispose();
            } else if (selection == 10) {
                cm.sendNext("这些是#r熊猫#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b撒尿#k(等级 1 ~ 30)\r\n#b站起来, 站立, 拥抱#k (等级 1 ~ 30)\r\n#b说话, 聊天#k (等级 10 ~ 30)\r\n#b玩耍#k (等级 20 ~ 30)\r\n#b嗯, 呃#k (等级 10 ~ 30)\r\n#b睡觉, 困了, 去睡觉#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 11) {
                cm.sendNext("这些是 #r恐龙男孩和恐龙女孩#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐#k (等级 1 ~ 30)\r\n#b坏, 不好, 愚蠢, 我讨厌你, 坏孩子, 坏女孩#k (等级 1 ~ 30)\r\n#b我爱你, 笨蛋#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b说话, 聊天(等级 10 ~ 30)\r\n#b微笑, 大笑#k (等级 1 ~ 30)\r\n#b可爱#k (等级 10 ~ 30)\r\n#b睡觉, 小睡, 困了#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 12) {
                cm.sendNext("这些是#r鲁道夫#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下#k（等级1~30）\r\n#b坏，不行，坏女孩，坏男孩#k（等级1~30）\r\n#b站起来，站立#k（等级1~30）\r\n#b笨蛋，讨厌你，傻瓜#k（等级1~30）\r\n#b圣诞快乐，圣诞快乐#k（等级11~30）\r\n#b我爱你#k（等级1~30）\r\n#b便便#k（等级1~30）\r\n#b说话，说，聊天#k（等级11~30）\r\n#b孤独，独自，沮丧，红鼻子#k（等级11~30）\r\n#b可爱#k（等级11~30）\r\n#b走，去#k（等级21~30）");
                cm.dispose();
            } else if (selection == 13) {
                cm.sendNext("这些是#r猴子#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐, 休息#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏孩子, 坏女孩#k (等级 1 ~ 30)\r\n#b站起来, 站立#k(等级 1 ~ 30)\r\n#b我爱你, 小便#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 11 ~ 30)\r\n#b玩, 唱歌#k (等级 11 ~ 30)\r\n#b睡觉, 困了, 上床#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 14) {
                cm.sendNext("这些是#r机器人#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下, 站立, 起立#k (等级 1 ~ 30)\r\n#b攻击, 坏, 不, 坏孩子#k (等级 1 ~ 30)\r\n#b愚蠢, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你, 好#k (等级 1 ~ 30)\r\n#b说话, 伪装#k (等级 11 ~ 30)");
                cm.dispose();
            } else if (selection == 15) {
                cm.sendNext("这些是 #r大象#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐, 休息#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏孩子, 坏女孩#k (等级 1 ~ 30)\r\n#b站起来, 站立, 起立#k (等级 1 ~ 30)\r\n#b我爱你, 小便#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天, 玩#k (等级 11 ~ 30)\r\n#b睡觉, 困了, 去睡觉#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 16) {
                cm.sendNext("这些是#r金猪#k的指令。指令旁边提到的等级是宠物需要达到的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏蛋, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎, 我爱你#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 11 ~ 30)\r\n#b爱我, 拥抱我#k (等级 11 ~ 30)\r\n#b睡觉, 困了, 上床#k (等级 21 ~ 30)\r\n#b印象深刻, 离开#k (等级 21 ~ 30)\r\n#b翻滚, 给我看钱#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 17) {
                cm.sendNext("这些是 #r企鹅#k 的指令。指令旁边提到的等级是宠物需要的等级才能执行该指令。\r\n#b坐#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏孩子, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b站起来, 站, 起来#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b拥抱, 抱抱我#k (等级 10 ~ 30)\r\n#b挥动, 举手#k (等级 10 ~ 30)\r\n#b睡觉#k (等级 20 ~ 30)\r\n#b亲吻, 亲亲, 亲一个#k (等级 20 ~ 30)\r\n#b飞#k (等级 20 ~ 30)\r\n#b可爱, 可爱的#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 18) {
                cm.sendNext("这些是 #r迷你雪人#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏男孩, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b跳舞, 舞动, 摇摆#k (等级 1 ~ 30)\r\n#b可爱, 可爱宝贝, 漂亮, 可爱#k (等级 1 ~ 30)\r\n#b我爱你, 喜欢你, 我的爱#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b睡觉, 小睡#k (等级 10 ~ 30)");
                cm.dispose();
            } else if (selection == 19) {
                cm.sendNext("这些是#rJr.巴尔洛#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#bliedown#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#biloveyou|mylove|likeyou#k (等级 1 ~ 30)\r\n#bcute|cutie|pretty|adorable#k (等级 1 ~ 30)\r\n#bpoop#k (等级 1 ~ 30)\r\n#bsmirk|crooked|laugh#k (等级 1 ~ 30)\r\n#bmelong#k (等级 11 ~ 30)\r\n#bgood|thelook|charisma#k (等级 11 ~ 30)\r\n#bspeak|talk|chat|say#k (等级 11 ~ 30)\r\n#bsleep|nap|sleepy#k (等级 11 ~ 30)\r\n#bgas#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 20) {
                cm.sendNext("这些是#r宝宝龙#k的指令。指令旁边提到的等级显示了宠物需要响应的等级要求。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b不|坏|坏女孩|坏男孩#k (等级 1 ~ 30)\r\n#b我爱你|爱你#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 1 ~ 30)\r\n#b可爱#k (等级 11 ~ 30)\r\n#b说话|聊天|说#k (等级 11 ~ 30)\r\n#b睡觉|困|上床#k (等级 11 ~ 30)");
                cm.dispose();
            } else if (selection == 21) {
                cm.sendNext("这些是#r绿/红/蓝龙#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐#k (等级 15 ~ 30)\r\n#b不|坏|坏女孩|坏男孩#k (等级 15 ~ 30)\r\n#b我爱你|爱你#k (等级 15 ~ 30)\r\n#b便便#k (等级 15 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 15 ~ 30)\r\n#b说话|聊天|说#k (等级 15 ~ 30)\r\n#b睡觉|困|上床#k (等级 15 ~ 30)\r\n#b变身#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 22) {
                cm.sendNext("这些是#r黑龙#k的指令。指令旁边提到的等级是宠物需要的等级才能响应。\r\n#b坐下#k (等级 15 ~ 30)\r\n#b不行|坏|坏女孩|坏男孩#k (等级 15 ~ 30)\r\n#b我爱你|爱你#k (等级 15 ~ 30)\r\n#b拉屎#k (等级 15 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 15 ~ 30)\r\n#b说话|聊天|说#k (等级 15 ~ 30)\r\n#b睡觉|困|上床#k (等级 15 ~ 30)\r\n#b可爱, 变化#k (等级 21 ~ 30)");
                cm.dispose();
            } else if (selection == 23) {
                cm.sendNext("这些是#r雪人#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b傻瓜, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你, 我的爱, 我喜欢你#k (等级 1 ~ 30)\r\n#b圣诞快乐#k (等级 1 ~ 30)\r\n#b可爱, 可爱的, 可爱, 漂亮#k (等级 1 ~ 30)\r\n#b坏蛋, 不, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说/睡觉, 困了, 去睡觉#k (等级 10 ~ 30)\r\n#b变身#k (等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 24) {
                cm.sendNext("这些是#r孙悟空#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#bsit#k(等级 1 ~ 30)\r\n#bno,bad,badgirl,badboy#k(等级 1 ~ 30)\r\n#bpoope#k(等级 1 ~ 30)\r\n#bcutie,adorable,cute,pretty#k(等级 1 ~ 30)\r\n#biloveyou,loveyou,luvyou,ilikeyou,mylove#k(等级 1 ~ 30)\r\n#btalk,chat,say/sleep,sleepy,gotobed#k(等级 10 ~ 30)\r\n#btransform#k(等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 25) {
                cm.sendNext("这些是#rJr. Reaper#k的指令。指令旁边提到的等级显示了宠物需要的等级才能执行该指令。\r\n#bsit#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#bplaydead, poop#k (等级 1 ~ 30)\r\n#btalk|chat|say#k (等级 1 ~ 30)\r\n#biloveyou, hug#k (等级 1 ~ 30)\r\n#bsmellmyfeet, rockout, boo#k (等级 1 ~ 30)\r\n#btrickortreat#k (等级 1 ~ 30)\r\n#bmonstermash#k (等级 1 ~ 30)");
                cm.dispose();
            } else if (selection == 26) {
                cm.sendNext("这些是#r水晶鲁道夫#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b不行|坏女孩|坏男孩#k (等级 1 ~ 30)\r\n#b呸|笑话#k(等级 1~30)\r\n#b变装|变身#k(等级 1 ~ 30) \r\n#b棒极了|感觉良好|啦啦啦#k(等级 1 ~ 30) \r\n#b爱你|嘿宝贝#k(等级 1 ~ 30) \r\n#b说话|说|聊天#k(等级 10 ~ 30) \r\n#b睡觉|困了|小睡|上床#k(等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 27) {
                cm.sendNext("这些是#rKino#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad|no|badgirl|badboy#k (等级 1 ~ 30)\r\n#bpoop#k (等级 1 ~ 30)\r\n#bsleep|nap|sleepy|gotobed#k(等级 1 ~ 30) \r\n#btalk|say|chat#k(等级 10 ~ 30) \r\n#biloveyou|mylove|likeyou#k(等级 10 ~ 30) \r\n#bmeh|bleh#k(等级 10 ~ 30) \r\n#bdisguise|change|transform#k(等级 20 ~ 30)");
                cm.dispose();
            } else if (selection == 28) {
                cm.sendNext("这些是#r白鸭#k的指令。指令旁边提到的等级显示了宠物需要的等级才能执行该指令。\r\n#b坐下#k（等级1~30）\r\n#b坏|不行|坏女孩|坏男孩#k（等级1~30）\r\n#b站起来|站立#k（等级1~30）\r\n#b拉屎#k（等级1~30）\r\n#b说话|聊天|说#k（等级1~30）\r\n#b拥抱#k（等级1~30）\r\n#b爱你#k（等级1~30）\r\n#b可爱#k（等级1~30）\r\n#b睡觉#k（等级1~30）\r\n#b聪明（等级10~30）\r\n#b跳舞#k（等级20~30）\r\n#b天鹅#k（等级20~30）");
                cm.dispose();
            } else if (selection == 29) {
                cm.sendNext("这些是#r粉豆#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#b坐下#k（等级1~30）\r\n#b坏|不行|坏女孩|坏男孩|便便#k（等级1~30）\r\n#b懒惰|傻瓜|我讨厌你说话|聊天|说|嘟囔我爱你|拥抱我|爱你#k（等级1~30）\r\n#b摇晃|音乐|魅力呃|笑话|嘘#k（等级20~30）\r\n#b去睡觉|睡觉|困了戳|臭|傻瓜|我讨厌你#k（等级20~30）\r\n#b砰砰#k（等级30）");
                cm.dispose();
            } else if (selection == 30) {
                cm.sendNext("这些是 #rPorcupine#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能执行该指令。\r\n#bsit#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#bhugcushion|sleep|knit|poop#k (等级 1 ~ 30)\r\n#bcomb|beach#k (等级 10 ~ 30)\r\n#btreeninja|dart#k (等级 20 ~ 30)");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("但是生命之水只在世界树的最底部出现一点点，所以这些宝宝们不能永远活着... 我知道，这非常不幸... 但即使它再次变成玩偶，它们也可以被带回生命，所以在你和它在一起的时候要对它好。");
        } else if (status == 3) {
            cm.sendNextPrev("哦，是的，当你给它们特殊指令时，它们会做出反应。你可以责备它们，爱护它们……这都取决于你如何照顾它们。它们害怕离开它们的主人，所以要对它们好，向它们展示爱。它们会很快感到伤心和孤独。");
            cm.dispose();
        } else if (status == 4) {
            cm.sendNextPrev("与宠物交谈，关注它，它的亲密度将提高，最终它的整体等级也会提高。随着亲密度的提高，宠物的整体等级也会很快提高。随着整体等级的提高，有一天宠物甚至可能会稍微像人一样说话，所以努力提升它。当然，这并不容易……");
        } else if (status == 5) {
            cm.sendNextPrev("它可能是一个活娃娃，但它们也有生命，所以它们也会感到饥饿。#b饱食度#k 显示宠物的饥饿程度。100 是最高值，数值越低，宠物就越饥饿。一段时间后，它甚至不会听从你的命令，而会主动进攻，所以要小心。");
        } else if (status == 6) {
            cm.sendNextPrev("没错！宠物不能吃普通的人类食物。相反，鲁塔比姆的一个叫做#b帕特里夏#k的泰迪熊出售#b宠物食物#k，所以如果你需要给你的宠物喂食，找到#b帕特里夏#k会是个好主意。最好提前购买食物，并在宠物真的饿得很厉害之前喂食。");
        } else if (status == 7) {
            cm.sendNextPrev("哦，如果你长时间不给宠物喂食，它会自己回家。你可以把它从家里带出来喂食，但这对宠物的健康不太好，所以尽量定期喂养它，这样它就不会降到那个程度了，好吗？我觉得这样就可以了。");
            cm.dispose();
        } else if (status == 8) {
            cm.sendNextPrev("一段时间后……没错，它们停止了移动。魔法效果消失，生命之水干涸后，它们就会变回木偶。但这并不意味着它们永远停止了，因为一旦你倒上生命之水，它们就会重新活过来。");
        } else if (status == 9) {
            cm.sendNextPrev("即使它有一天再次移动，看到它们完全停止还是很伤心的。请在它们还活着并移动时对它们好一点。也要好好喂养它们。知道有一些活着的东西只跟随并听从你，是不是很好呢？");
            cm.dispose();
        }
    }
}