/*
	NPC Name: 		Liao Fan
	Map(s): 		Mount Song: Mahavira Hall (702100000)
	Description: 	Monk NPC, pranks the player by turning them bald
	Creator : 		[ArthurZhu1992]
*/
let status = 0;
let playerHair = 0;

function start() {
    console.log("status: " + status)
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (status >= 0 && mode === -1) {
        cm.dispose();
        return;
    }

    if (status === -1) {
        status++
    } else if (mode === 1 && selection === -1 && type === 0) {
        status++;
    } else if (mode === 1 && selection === -1 && type === 1) {
        status++;
    } else if (mode === 0 && selection === -1 && type === 1) {
        status++;
    } else {
        status--;
    }


    if (status === 0) {
        // Save current hair
        playerHair = cm.getPlayer().getHair();
        cm.sendNext("Amitabha! Traveler, I see a shadow of worry upon your face. Is there something troubling your mind?");
    } else if (status == 1) {
        cm.sendNextPrev("Life is full of tribulations, but one must not cling to them. Since you look so troubled, why not consider ordaining as a monk to sever those '3,000 strands of worry'?");
    } else if (status == 2) {
        cm.sendNextPrev("Do not rush to answer. Let me tell you a story: Once there was a traveler just as troubled as you. He chose to join the temple, took the Buddhist name 'End-of-Woe', and lived in peace ever after.");
    } else if (status == 3) {
        cm.sendNextPrev("Hahaha, I am merely jesting. However, since you are here, why not experience the life of a monk for a moment? Let's start with the tonsure—the ritual of shaving the head.");
    } else if (status == 4) {
        cm.sendNextPrev("Do you wish to 'cut the threads of the past and let your sorrows drift away with the wind'? I see great potential in you. A bald head is the first step toward enlightenment!");
    } else if (status == 5) {
        cm.sendYesNo("Since you seem determined, I shall assist you. But be warned: once the hair is gone, there is no turning back... Are you sure you want to shave your head?");
    } else if (status == 6) {
        if (mode === 1 && selection === -1 && type === 1) { // Player chose Yes
            //Determine gender
            let baldHair;// Bald hair style ID
            // Turn player into bald head 30437 Boy with big bald head
            if (cm.getPlayer().getGender() === 0) {
                baldHair = 30437;
            } else {
                // Turn player into bald head 31437 Girl with big bald head
                baldHair = 31437;
            }
            cm.setHair(baldHair);
            cm.sendNext("Amitabha! The ritual is complete! Your worldly worries have been severed, and your mind is now as clear as a polished mirror.");
        } else {
            cm.sendNext("It seems you are not yet ready to leave the secular world. No matter; spiritual cultivation cannot be forced. It requires the right destiny.");
            cm.dispose();
        }
    } else if (status == 7) {
        cm.sendNextPrev("Now that you are bald, how do you feel? Don't you feel refreshed, as if all your burdens have vanished?");
    } else if (status == 8) {
        cm.sendNextPrev("Haha, just a joke! Don't take it too seriously. This bald look is just a temporary experience, though I must say, it suits you quite well!");
    } else if (status == 9) {
        cm.sendNextPrev("If you wish to return to your original look, you can visit a hair stylist, or simply wait for a while—your hair will eventually grow back.");
    } else if (status == 10) {
        cm.sendNextPrev("Well, I have my morning lessons to attend to. Remember: worries are often self-created. To let go is to find liberation.");
    } else if (status == 11) {
        cm.sendNextPrev("Amitabha, bless you! Take care, traveler, and may we meet again if fate allows!");
        cm.dispose();
    } else {
        cm.dispose();
    }
}
