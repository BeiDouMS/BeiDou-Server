/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Shati
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Assistant Hairdresser

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mhair_r = Array(30150, 30170, 30180, 30320, 30330, 30410, 30460, 30680, 30800, 30820, 30900);
var fhair_r = Array(31090, 31190, 31330, 31340, 31400, 31420, 31520, 31620, 31650, 31660, 34000);
var hairnew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        if (type == 7) {
            cm.sendNext("我猜你还不准备做出改变。等你准备好了再告诉我吧！");
        }

        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("嘿！我是沙提，是马兹拉的学徒。如果你有 #b阿里安特发型券(REG)#k 或 #b阿里安特染发券(REG)#k，你愿意让我给你做头发吗？\r\n#L0#理发：#i5150026##t5150026##l\r\n#L1#染发：#i5151021##t5151021##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 1;
                hairnew = Array();
                if (cm.getChar().getGender() == 0) {
                    for (var i = 0; i < mhair_r.length; i++) {
                        pushIfItemExists(hairnew, mhair_r[i] + parseInt(cm.getChar().getHair()
                            % 10));
                    }
                }
                if (cm.getChar().getGender() == 1) {
                    for (var i = 0; i < fhair_r.length; i++) {
                        pushIfItemExists(hairnew, fhair_r[i] + parseInt(cm.getChar().getHair()
                            % 10));
                    }
                }
                cm.sendYesNo("如果您使用REG优惠券，您的发型将会被随机更换成全新的造型。您还将获得我设计的一些全新发型，这些发型是VIP优惠券无法获得的。您想要使用#b阿里安特发型优惠券(REG)#k来获得一个华丽的新造型吗？");
            } else if (selection == 1) {
                beauty = 2;
                haircolor = Array();
                var current = parseInt(cm.getChar().getHair()
                    / 10) * 10;
                for (var i = 0; i < 8; i++) {
                    pushIfItemExists(haircolor, current + i);
                }
                cm.sendYesNo("如果您使用普通的优惠券，您的发色将会随机变成一种新的颜色。您确定要使用 #b#t5151021##k 并随机改变您的发色吗？");
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                if (cm.haveItem(5150026) == true) {
                    cm.gainItem(5150026, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("享受你的新发型吧！");
                } else {
                    cm.sendNext("只有你带来了优惠券，我才能帮你改变发型。你不会忘了吧？");
                }
            }
            if (beauty == 2) {
                if (cm.haveItem(5151021) == true) {
                    cm.gainItem(5151021, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("享受你的新发色吧！");
                } else {
                    cm.sendNext("只有你拿来了发型券，我才能帮你换发型。你不会忘了那个吧？");
                }
            }
        }
    }
}