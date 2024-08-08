/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Vard
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Ariant Plastic Surgery

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/

var status = 0;
var beauty = 0;
var mface_v = Array(20000, 20004, 20005, 20012, 20013, 20031);
var fface_v = Array(21000, 21003, 21006, 21009, 21012, 21024);
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
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendSimple("啊，欢迎来到阿里安特整形中心！您想将您的脸变成全新的样子吗？通过使用#b#t5152030##k或者#b#t5152047##k，我可以让您的脸变得更好看！\r\n#L1#整形手术：#i5152030##t5152030##l\r\n#L2#美瞳：#i5152047##t5152047##l\r\n#L3#一次性美瞳：#i5152101#（任何颜色）#l");
        } else if (status == 1) {
            if (selection == 1) {
                beauty = 0;

                facenew = Array();
                if (cm.getChar().getGender() == 0) {
                    for (var i = 0; i < mface_v.length; i++) {
                        pushIfItemExists(facenew, mface_v[i] + cm.getChar().getFace()
                            % 1000 - (cm.getChar().getFace()
                                % 100));
                    }
                }
                if (cm.getChar().getGender() == 1) {
                    for (var i = 0; i < fface.length; i++) {
                        pushIfItemExists(facenew, fface[i] + cm.getChar().getFace()
                            % 1000 - (cm.getChar().getFace()
                                % 100));
                    }
                }
                cm.sendStyle("嗯......即使在遮蔽和燃烧的沙漠下，美丽的面孔也会发光。选择你想要的面孔，我会拿出我杰出的技艺来进行美化。", facenew);
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
                cm.sendStyle("我们将用新的镜头使您的眼睛更加明亮，与沙漠中闪闪发光的沙子相匹配，沙漠高兴地拥抱着宫殿的屋顶。选择您想要使用的...", colors);
            } else if (selection == 3) {
                beauty = 3;
                if (cm.getPlayer().getGender() == 0) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 20000;
                }
                if (cm.getPlayer().getGender() == 1) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 21000;
                }

                colors = Array();
                for (var i = 0; i < 8; i++) {
                    if (cm.haveItem(5152100 + i)) {
                        pushIfItemExists(colors, current + 100 * i);
                    }
                }

                if (colors.length == 0) {
                    cm.sendOk("你没有任何一次性化妆镜片可供使用。");
                    cm.dispose();
                    return;
                }

                cm.sendStyle("你想戴什么样的眼镜？请选择您喜欢的风格。", colors);
            }
        } else if (status == 2) {
            cm.dispose();

            if (beauty == 0) {
                if (cm.haveItem(5152030) == true) {
                    cm.gainItem(5152030, -1);
                    cm.setFace(facenew[selection]);
                    cm.sendOk("享受你的新面容吧！");
                } else {
                    cm.sendNext("嗯...你似乎没有这家医院的专属优惠券。没有优惠券，恐怕我不能为你办理。");
                }
            } else if (beauty == 1) {
                if (cm.haveItem(5152047) == true) {
                    cm.gainItem(5152047, -1);
                    cm.setFace(colors[selection]);
                    cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有这个地方专门的优惠券。很抱歉要说这个，但没有优惠券，你就不能进行整形手术了...");
                }
            } else if (beauty == 3) {
                var color = (colors[selection] / 100) % 10 | 0;

                if (cm.haveItem(5152100 + color)) {
                    cm.gainItem(5152100 + color, -1);
                    cm.setFace(colors[selection]);
                    cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
                } else {
                    cm.sendOk("对不起，但我觉得你现在没有我们的化妆镜片优惠券。没有优惠券，恐怕我不能为你做。");
                }
            }
        }
    }
}