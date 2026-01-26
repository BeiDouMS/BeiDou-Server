/**
 * 名称：快速转职
 * 功能：可快速转职：冒险家、战神、骑士团
 *      不开放其他职业可屏蔽37行>>>38行
 * 作者：Jason
 * 版本：1.0
 * 日期：20260119
 */
 
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendOk("天气很好哦~~如果你改变想法记的随时来找我。祝你好运！");
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            cm.sendNext("嗨，我是 #b全职业转职官#k 我可以帮助你快速转职哦~~！");
        } else if (status == 1) {
            var jobid = cm.getJobId();
            var where = "你想转职为一名？\r\n#b";
            
            if (jobid % 100 == 0) {
                switch (Math.floor(jobid / 100)) {
                    case 0:
                        where += "#L100#战士#l #L200#法师#l #L300#弓箭手#l #L400#飞侠#l #L500#海盗#l \r\n";
                        //where += "#L1100#魂骑士#l #L1200#炎术士#l #L1300#风灵使者#l #L1400#夜行者#l #L1500#奇袭者#l\r\n";
                        //where += "#L2100#战神#l \r\n";
                        break;
                    case 1:
                        where += "#L110#勇士#l #L120#准骑士#l #L130#枪战士#l \r\n";
                        break;
                    case 2:
                        where += "#L210#法师(火,毒)#l #L220#法师(冰,雷)#l #L230#牧师#l \r\n";
                        break;
                    case 3:
                        where += "#L310#猎手#l #L320#弩弓手#l \r\n";
                        break;
                    case 4:
                        where += "#L410#刺客#l #L420#侠客#l \r\n";
                        break;
                    case 5:
                        where += "#L510#拳手#l #L520#火枪手#l \r\n";
                        break;
					case 10:
                        where += "#L1100#魂骑士#l #L1200#炎术士#l #L1300#风灵使者#l #L1400#夜行者#l #L1500#奇袭者#l\r\n";
                        break;
                    case 11:
                        where += "#L1110#魂骑士(二转)#l \r\n";
                        break;
                    case 12:
                        where += "#L1210#炎术士(二转)#l \r\n";
                        break;
                    case 13:
                        where += "#L1310#风灵使者(二转)#l \r\n";
                        break;
                    case 14:
                        where += "#L1410#夜行者(二转)#l \r\n";
                        break;
                    case 15:
                        where += "#L1510#奇袭者(二转)#l \r\n";
                        break;
                    case 20:
                        where += "#L2100#战神#l \r\n";
                        break;
                    case 21:
                        where += "#L2110#战神(二转)#l \r\n";
                        break;
                }
            } else if (jobid % 100 != 0) {
                where += "#L" + (jobid + 1) + "#我要进行#r" + (jobid % 10 + 3) + "#k转#l \r\n";
            }
            
            cm.sendSimple(where);
        } else if (status == 2) {
            var changeto = selection;
            var jobid = cm.getJobId();
            if (jobid % 1000 == 0) {
                if (cm.getChar().getLevel() >= 8 && changeto == 200 || cm.getPlayer().getLevel() >= 10) {
                    cm.changeJobById(changeto);
					cm.dropMessage(0,"恭喜 [" + cm.getPlayer() + "] 快速一转！"); 
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 100 == 0) {
                if (cm.getChar().getLevel() >= 30 && cm.getJobId() == 200) {
                    cm.changeJobById(changeto);
                } else if (cm.getChar().getLevel() >= 30) {
                    cm.changeJobById(changeto);
					cm.dropMessage(0,"恭喜 [" + cm.getPlayer() + "] 快速二转！"); 
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 10 == 0) {
                if (cm.getChar().getLevel() >= 70) {
                    cm.changeJobById(changeto);
					cm.dropMessage(0,"恭喜 [" + cm.getPlayer() + "] 快速三转！"); 
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 10 == 1) {
                if (jobid / 1000 >= 1 && jobid / 1000 < 2) {
                    cm.sendOk("骑士团共3转 已完成！");
                } else if (cm.getChar().getLevel() >= 120) {
                    cm.changeJobById(changeto);
					cm.dropMessage(0,"恭喜 [" + cm.getPlayer() + "] 快速四转！"); 
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else {
                cm.sendOk("所有转职已经完成！");
            }
            
            cm.dispose();
        } 
    }
}