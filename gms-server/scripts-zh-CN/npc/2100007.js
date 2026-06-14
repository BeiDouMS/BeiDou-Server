/* Author: aaroncsn <MapleSea Like, Incomplete, Needs skin id>
    NPC Name:       Laila
    Map(s):         The Burning Road: Ariant(2600000000)
    Description:    Skin Care Specialist
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
            cm.sendNext("呵呵呵~ 欢迎，欢迎。欢迎来到阿里安特护肤中心。你来到的是一家知名护肤店，就连王妃本人也经常光顾这里。如果你带着#b阿里安特护肤券#k，剩下的事就交给我们吧。今天要不要让我们帮你护理一下肌肤呢？");
        } else if (status == 1) {
            cm.sendStyle("通过我们的专业机器，你可以提前看到护理后的效果。你想做哪一种护肤护理呢？请选择你喜欢的肤色。", skin);
        } else if (status == 2) {
            cm.dispose();
            if (cm.haveItem(5153007) == true) {
                cm.gainItem(5153007, -1);
                cm.setSkin(skin[selection]);
                cm.sendOk("好好享受你焕然一新的肌肤吧！");
            } else {
                cm.sendNext("嗯……你身上好像没有我们的护肤券。没有护肤券的话，我不能为你提供护理服务。");
            }
        }
    }
}
