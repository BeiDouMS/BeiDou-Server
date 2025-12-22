/*
    by ziqiming
 */

var status;
var sel;

var create_guild_cost = 0; //创建家族金额
var expand_guild_cost = 0;  //扩展人数需求金额
const GameConfig = Java.type('org.gms.config.GameConfig');
function start() {
    create_guild_cost =  GameConfig.getServerInt('create_guild_cost')
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
       if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if(status == 0){
            var text = "欢迎来到家族中心，你现在想做什么呢？\r\n";
            text += "#b"
            text += "#L0#创建家族#l\r\n";
            text += "#L1#解散家族#l\r\n";
            text += "#L2#增加家族成员人数上限#l\r\n"
            cm.sendSimple(text);

        } else if (status == 1) {

            sel = selection;
            if (selection == 0) {
                if (cm.getPlayer().getGuildId() > 0) {
                    cm.sendOk("你已经拥有家族了，不能再创建家族。");
                    cm.dispose();
                } else {
                    cm.sendYesNo(`创建一个新的家族需要 #b ${create_guild_cost} 金币#k，你确定继续创建一个新的家族吗？`);
                }
            } else if (selection == 1) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                    cm.sendOk("你还没有家族！或者\r 你不是族长，因此你不能解散该家族.");
                    cm.dispose();
                } else {
                    cm.sendYesNo("你确定真的要解散你的家族？当解散后你将不能恢复所有家族相关资料以及GP的数值，是否继续？");
                }
            } else if (selection == 2) {

                //GS有单独处理，家族人数大于30人增加扣费金额，因此需获取需要扣的金币数量
                var Guild = Java.type("org.gms.net.server.guild.Guild");
                var memeber = cm.getPlayer().getGuild().getCapacity();
                expand_guild_cost = Guild.getIncreaseGuildCost(memeber)
                
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() > 2) {
                    cm.sendOk("你不是家族管理者，因此你将不能增加家族成员的人数上限.");
                    cm.dispose();
                } else {
                    cm.sendYesNo(`家族成员上限增加 #r5#k 位需支付 #r${expand_guild_cost}金币。#k你确定要继续吗？`);
                }
            }
        } else if (status == 2) {
            if (sel == 0) {
                cm.getPlayer().genericGuildMessage(1);
            } else if (sel == 1) {
                cm.getPlayer().disbandGuild();
            } else if (sel == 2) {
                cm.getPlayer().increaseGuildCapacity();
            }

            cm.dispose();
            
        }else{
            cm.dispose();
        }
    }
}
