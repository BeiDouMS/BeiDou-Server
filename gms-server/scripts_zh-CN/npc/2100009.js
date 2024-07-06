/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Aldin
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Ariant Plastic Surgery

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mface_r = Array(20001, 20003, 20009, 20010, 20025, 20031);
var fface_r = Array(21002, 21009, 21011, 21013, 21016, 21029, 21030);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];

        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
    }
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        if (type == 7) {
            cm.sendNext("我明白了...慢慢考虑，看看你是否真的想要。当你下定决心时告诉我。");
        }

        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendSimple("嗨，我是这里的整容助理医生。用一个#b#t5152029##k或者一个#b#t5152048##k，我可以让它变得完美，相信我。啊，别忘了，手术后的结果是随机的！那么，你要做哪个呢？\r\n#L1#整形手术：#i5152029##t5152029##l\r\n#L2#美瞳：#i5152048##t5152048##l");
        } else if (status == 1) {
            if (selection == 1) {
                beauty = 0;

                facenew = Array();
                if (cm.getChar().getGender() == 0) {
                    for (var i = 0; i < mface_r.length; i++) {
                        pushIfItemExists(facenew, mface_r[i] + cm.getChar().getFace()
                            % 1000 - (cm.getChar().getFace()
                                % 100));
                    }
                }
                if (cm.getChar().getGender() == 1) {
                    for (var i = 0; i < fface_r.length; i++) {
                        pushIfItemExists(facenew, fface_r[i] + cm.getChar().getFace()
                            % 1000 - (cm.getChar().getFace()
                                % 100));
                    }
                }
                cm.sendYesNo("如果你使用普通的优惠券，你的脸可能会变成一个随机的新样子...你还想用#b#t5152029##k来做吗？");
            } else if (selection == 2) {
                beauty = 1;
                if (cm.getPlayer().getGender() == 0) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 20000;
                }
                if (cm.getPlayer().getGender() == 1) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 21000;
                }
                colors = Array();
                pushIfItemsExists(colors, [current, current + 100, current + 300, current + 600, current + 700]);
                cm.sendYesNo("如果你使用普通优惠券，你将获得一副随机的化妆隐形眼镜。你打算使用#b#t5152048##k，真的改变你的眼睛吗？");
            }
        } else if (status == 2) {
            cm.dispose();

            if (beauty == 0) {
                if (cm.haveItem(5152029) == true) {
                    cm.gainItem(5152029, -1);
                    cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
                    cm.sendOk("享受你的新面容吧！");
                } else {
                    cm.sendNext("嗯...看起来你没有这个地方特定的优惠券...很抱歉要说这个，但没有优惠券的话，你就不能进行整形手术了。");
                }
            } else if (beauty == 1) {
                if (cm.haveItem(5152048)) {
                    cm.gainItem(5152048, -1);
                    cm.setFace(colors[Math.floor(Math.random() * colors.length)]);
                    cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有这个地方专门的优惠券。很抱歉要说这个，但没有优惠券，你就不能进行整形手术了...");
                }
            }
        }
    }
}