/* Author: aaroncsn <MapleSea Like>
    NPC Name:       Mazra
    Map(s):         The Burning Road: Ariant(2600000000)
    Description:    Hair Salon Owner

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mhair_v = Array(30150, 30170, 30180, 30320, 30330, 30410, 30460, 30820, 30900);
var fhair_v = Array(31040, 31090, 31190, 31330, 31340, 31400, 31420, 31620, 31660);
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
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("哈哈哈……在沙漠里还愿意打理发型的人，确实需要不少风格和魅力。像你这样的人……如果你有#b阿里安特美发券（VIP）#k或#b阿里安特染发券（VIP）#k，我可以让你的头发焕然一新。\r\n#L0#理发：#i5150027##t5150027##l\r\n#L1#染发：#i5151022##t5151022##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 1;
                hairnew = Array();
                if (cm.getChar().getGender() == 0) {
                    for (var i = 0; i < mhair_v.length; i++) {
                        pushIfItemExists(hairnew, mhair_v[i] + parseInt(cm.getChar().getHair()
                            % 10));
                    }
                }
                if (cm.getChar().getGender() == 1) {
                    for (var i = 0; i < fhair_v.length; i++) {
                        pushIfItemExists(hairnew, fhair_v[i] + parseInt(cm.getChar().getHair()
                            % 10));
                    }
                }
                cm.sendStyle("哈哈哈~ 只要有#b阿里安特美发券（VIP）#k，就可以改变你的发型。选择你喜欢的新发型，剩下的就交给我吧。", hairnew);
            } else if (selection == 1) {
                beauty = 2;
                haircolor = Array();
                var current = parseInt(cm.getChar().getHair()
                    / 10) * 10;
                for (var i = 0; i < 8; i++) {
                    pushIfItemExists(haircolor, current + i);
                }
                cm.sendStyle("偶尔换个发色也不错……这很有趣。带上#b阿里安特染发券（VIP）#k，选择你想要的新发色，就让我玛兹拉来为你染发吧。", haircolor);
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                if (cm.haveItem(5150027) == true) {
                    cm.gainItem(5150027, -1);
                    cm.setHair(hairnew[selection]);
                    cm.sendOk("好好享受你焕然一新的发型吧！");
                } else {
                    cm.sendNext("我应该说过了，需要美发券我才能为你的头发施展魔法。再检查一下吧。");
                }
            }
            if (beauty == 2) {
                if (cm.haveItem(5151022) == true) {
                    cm.gainItem(5151022, -1);
                    cm.setHair(haircolor[selection]);
                    cm.sendOk("好好享受你焕然一新的发色吧！");
                } else {
                    cm.sendNext("我应该说过了，需要染发券我才能为你的头发施展魔法。再检查一下吧。");
                }
            }
        }
    }
}