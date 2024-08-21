/*
NPC:        Muirhat - Nautilus' Port
Created By: Kevin
Function:   Ask the player if they want to be warped to Black Magician's Disciple
*/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("你想要被传送到黑魔法师的弟子那里吗？");
        } else if (status == 1) {
            if (mode == 1) { 
                cm.warp(912000000, 0);
            } else { 
                cm.sendOk("黑魔法师及其追随者。凯琳和诺提勒斯船员。他们会互相追逐，直到其中一个消失，这是肯定的。");
            }
            cm.dispose();
        }
    }
}
