/**
 * 名称：快速转职
 * 功能：可快速转职：冒险家、战神、骑士团
 *      不开放其他职业可屏蔽37行>>>38行
 * 作者：Jason
 * 版本：2.1
 * 日期：20260119
 * 修改：添加4转技能等级初始化（修正职业ID）
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
                    cm.dropMessage(5,"【转职系统】玩家 [" + cm.getPlayer() + "] 快速一转");
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 100 == 0) {
                if (cm.getChar().getLevel() >= 30 && cm.getJobId() == 200) {
                    cm.changeJobById(changeto);
                } else if (cm.getChar().getLevel() >= 30) {
                    cm.changeJobById(changeto);
                    cm.dropMessage(5,"【转职系统】玩家 [" + cm.getPlayer() + "] 快速二转");
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 10 == 0) {
                if (cm.getChar().getLevel() >= 70) {
                    cm.changeJobById(changeto);
                    cm.dropMessage(5,"【转职系统】玩家 [" + cm.getPlayer() + "] 快速三转");
                } else {
                    cm.sendOk("你还没有满足转职条件！");
                }
            } else if (jobid % 10 == 1) {
                if (jobid / 1000 >= 1 && jobid / 1000 < 2) {
                    cm.sendOk("骑士团共3转 已完成！");
                } else if (cm.getChar().getLevel() >= 120) {
                    cm.changeJobById(changeto);
                    cm.dropMessage(5,"【转职系统】玩家 [" + cm.getPlayer() + "] 快速四转");
                    
                    // 四转后自动初始化职业技能等级（使用四转后的职业ID）
                    initializeFourthJobSkills(changeto);
                    
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

// 初始化四转职业技能（等级0，最高10级）
function initializeFourthJobSkills(jobId) {
    var player = cm.getPlayer();
    var skills = [];
    
    // 根据四转职业ID获取对应的技能列表
    switch (jobId) {
        // 战士系列 - 四转
        case 112: // 英雄
            skills = [1121000, 1121001, 1121002, 1121003, 1121004, 1121005, 1121006, 1121008, 1121010];
            break;
        case 122: // 圣骑士
            skills = [1221000, 1221001, 1221002, 1221003, 1221004, 1221005, 1221006, 1221007, 1221009, 1221011];
            break;
        case 132: // 黑骑士
            skills = [1321000, 1321001, 1321002, 1321003, 1321004, 1321005, 1321006, 1321007, 1321009];
            break;
        
        // 法师系列 - 四转
        case 212: // 火毒魔导师
            skills = [2121000, 2121001, 2121002, 2121003, 2121004, 2121005, 2121006, 2121007, 2121008];
            break;
        case 222: // 冰雷魔导师
            skills = [2221000, 2221001, 2221002, 2221003, 2221004, 2221005, 2221006, 2221007, 2221008];
            break;
        case 232: // 主教
            skills = [2321000, 2321001, 2321002, 2321003, 2321004, 2321005, 2321006, 2321007, 2321008, 2321009];
            break;
        
        // 弓箭手系列 - 四转
        case 312: // 神射手
            skills = [3121000, 3121002, 3121003, 3121004, 3121005, 3121006, 3121007, 3121008];
            break;
        case 322: // 箭神
            skills = [3221000, 3221001, 3221002, 3221003, 3221004, 3221005, 3221006, 3221007];
            break;
        
        // 飞侠系列 - 四转
        case 412: // 隐士
            skills = [4121000, 4121001, 4121002, 4121003, 4121004, 4121005, 4121006, 4121007, 4121008, 4121009];
            break;
        case 422: // 侠盗
            skills = [4221000, 4221001, 4221002, 4221003, 4221004, 4221005, 4221006, 4221007, 4221008];
            break;
        
        // 海盗系列 - 四转
        case 512: // 冲锋队长
            skills = [5121000, 5121001, 5121002, 5121003, 5121004, 5121005, 5121006, 5121007, 5121008, 5121009];
            break;
        case 522: // 船长
            skills = [5221000, 5221001, 5221002, 5221003, 5221004, 5221005, 5221006, 5221007, 5221008, 5221009, 5221010];
            break;
        
        // 骑士团系列 - 四转
        case 1112: // 魂骑士（四转）
            skills = [11121000, 11121001, 11121002, 11121003, 11121004];
            break;
        case 1212: // 炎术士（四转）
            skills = [12121000, 12121001, 12121002, 12121003, 12121004];
            break;
        case 1312: // 风灵使者（四转）
            skills = [13121000, 13121001, 13121002, 13121003, 13121004];
            break;
        case 1412: // 夜行者（四转）
            skills = [14121000, 14121001, 14121002, 14121003, 14121004];
            break;
        case 1512: // 奇袭者（四转）
            skills = [15121000, 15121001, 15121002, 15121003, 15121004];
            break;
        
        // 战神系列 - 四转
        case 2112: // 战神（四转）
            skills = [21121000, 21121001, 21121002, 21121003, 21121004];
            break;
        
        default:
            // 尝试自动获取该职业的所有技能
            getAllJobSkills(jobId);
            return;
    }
    
    // 初始化每个技能
    var skillCount = 0;
    for (var i = 0; i < skills.length; i++) {
        if (initializeSkill(skills[i])) {
            skillCount++;
        }
    }
    
    if (skillCount > 0) {
        cm.dropMessage(5, "【技能系统】已初始化 " + skillCount + " 个四转技能，等级0/10");
    } else {
        cm.dropMessage(5, "【技能系统】所有四转技能已初始化或请手动学习");
    }
}

// 初始化单个技能
function initializeSkill(skillId) {
    try {
        var SkillFactory = Java.type('org.gms.client.SkillFactory');
        var skill = SkillFactory.getSkill(skillId);
        
        if (skill == null) {
            return false;
        }
        
        var player = cm.getPlayer();
        
        // 检查玩家是否已有该技能
        var currentLevel = player.getSkillLevel(skill);
        
        // 如果技能等级为0，则设置为0级（可学习）
        if (currentLevel <= 0) {
            // 设置技能等级为0，最高等级为10
            player.changeSkillLevel(skill, 0, 10, -1);
            return true;
        }
        return false;
        
    } catch (e) {
        return false;
    }
}

// 获取职业的所有技能（备用方法）
function getAllJobSkills(jobId) {
    try {
        var player = cm.getPlayer();
        var SkillFactory = Java.type('org.gms.client.SkillFactory');
        
        // 获取该职业的所有技能
        var allSkills = SkillFactory.getAllSkills();
        var skillCount = 0;
        
        // 遍历所有技能，找出属于该职业的四转技能
        for (var i = 0; i < allSkills.size(); i++) {
            var skill = allSkills.get(i);
            var skillJob = skill.getJobId();
            
            // 判断技能是否属于当前职业且是四转技能
            if (skillJob == jobId && skill.isFourthJob()) {
                var currentLevel = player.getSkillLevel(skill);
                if (currentLevel <= 0) {
                    player.changeSkillLevel(skill, 0, 10, -1);
                    skillCount++;
                }
            }
        }
        
        if (skillCount > 0) {
            cm.dropMessage(5, "【技能系统】已初始化 " + skillCount + " 个四转技能，等级0/10");
        }
        
    } catch (e) {
        // 忽略错误
    }
}