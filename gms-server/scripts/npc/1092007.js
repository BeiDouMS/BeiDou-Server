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
            cm.sendYesNo("Do you want to be warped to the Black Magician's Disciple?");
        } else if (status == 1) {
            if (mode == 1) { // Player chose "Yes"
                cm.warp(912000000, 0);
            } else { // Player chose "No"
                cm.sendOk("The Black Magician and his followers. Kyrin and the Crew of Nautilus. \n They'll be chasing one another until one of them doesn't exist, that's for sure.");
            }
            cm.dispose();
        }
    }
}
