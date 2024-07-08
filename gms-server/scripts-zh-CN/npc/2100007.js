/* Author: aaroncsn <MapleSea Like, Incomplete, Needs skin id>
	NPC Name: 		Laila
	Map(s): 		The Burning Road: Ariant(2600000000)
	Description: 	Skin Care Specialist
*/

var status = 0;
var skin = Array(0, 1, 2, 3, 4);

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
            cm.sendNext("Hohoh~ 欢迎欢迎。欢迎来到阿里安特护肤中心。你已经踏入了一家著名的护肤店，甚至连女王本人都经常光顾这个地方。如果你带着 #b阿里安特护肤优惠券#k，我们会照顾好你的其余事项。今天让我们来护理一下你的肌肤吧？");
        } else if (status == 1) {
            cm.sendStyle("With our specialized machine, you can see yourself after the treatment in advance. What kind of skin-treatment would you like to do? Choose the style of your liking...", skin);
        } else if (status == 2) {
            cm.dispose();
            if (cm.haveItem(5153007) == true) {
                cm.gainItem(5153007, -1);
                cm.setSkin(skin[selection]);
                cm.sendOk("享受你的新肤色吧！");
            } else {
                cm.sendNext("嗯...我觉得你没有我们的护肤券。没有券，我就不能给你护理服务。");
            }
        }
    }
}