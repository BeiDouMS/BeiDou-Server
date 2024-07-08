/* @Author Lerk
 * @Author Ronan
 *
 * Guardian Statue - Sharenian: Fountain of the Wiseman (990000500)
 * 
 * Guild Quest Stage 3
 */

function clearStage(stage, eim) {
    eim.setProperty("stage" + stage + "clear", "true");
    eim.showClearEffect(true);

    eim.giveEventPlayersStageReward(stage);
}

function start() {
    if (cm.getPlayer().getMap().getReactorByName("watergate").getState() > 0) {
        cm.sendOk("干得好。你可以继续进行下一阶段了。");
        cm.dispose();
        return;
    }

    var eim = cm.getPlayer().getEventInstance();
    if (eim == null) {
        cm.warp(990001100);
    } else {
        if (cm.isEventLeader()) {
            var currentCombo = eim.getProperty("stage3combo");
            if (currentCombo == null || currentCombo === "reset") {
                var newCombo = makeCombo();
                eim.setProperty("stage3combo", newCombo);
                //cm.playerMessage("Debug: " + newCombo);
                eim.setProperty("stage3attempt", "1");
                cm.sendOk("这个喷泉守护着通往王座房间的秘密通道。在这个区域里向家臣们献上物品以继续前行。家臣们会告诉你你的献礼是否被接受，如果没有，哪些家臣感到不满。你有七次尝试的机会。祝你好运。");
            } else {
                var attempt = parseInt(eim.getProperty("stage3attempt"));
                var combo = parseInt(currentCombo);
                var guess = getGroundItems();
                if (guess != null) {
                    if (combo == guess) {
                        cm.getPlayer().getMap().getReactorByName("watergate").forceHitReactor(1);
                        clearStage(3, eim);
                        cm.getGuild().gainGP(25);

                        removeGroundItems();
                        cm.sendOk("干得好。你可以继续进行下一阶段了。");
                    } else {
                        if (attempt < 7) {
                            var comboItems = [0, 0, 0, 0];
                            var guessItems = [0, 0, 0, 0];

                            var correct = 0, incorrect, unknown = 0;
                            for (var i = 0; i < 4; i++) {
                                var guessIdx = Math.floor(guess / Math.pow(10, i)) % 10;
                                var comboIdx = Math.floor(combo / Math.pow(10, i)) % 10;

                                if (guessIdx == comboIdx) {
                                    correct++;
                                } else {
                                    (guessItems[guessIdx])++;
                                    (comboItems[comboIdx])++;
                                }
                            }

                            for (var i = 0; i < 4; i++) {
                                var diff = guessItems[i] - comboItems[i];
                                if (diff > 0) {
                                    unknown += diff;
                                }
                            }

                            incorrect = 4 - correct - unknown;

                            var string = "";
                            //cm.playerMessage("Results - Correct: " + results[0] + " | Incorrect: " + results[1] + " | Unknown: " + results[2]);
                            if (correct != 0) {
                                if (correct == 1) {
                                    string += "1 vassal is pleased with their offering.\r\n";
                                } else {
                                    string += correct + " vassals are pleased with their offerings.\r\n";
                                }
                            }
                            if (incorrect != 0) {
                                if (incorrect == 1) {
                                    string += "1 vassal has received an incorrect offering.\r\n";
                                } else {
                                    string += incorrect + " vassals have received incorrect offerings.\r\n";
                                }
                            }
                            if (unknown != 0) {
                                if (unknown == 1) {
                                    string += "1 vassal has received an unknown offering.\r\n";
                                } else {
                                    string += unknown + " vassals have received unknown offerings.\r\n";
                                }
                            }
                            string += "This is your ";
                            switch (attempt) {
                                case 1:
                                    string += "1st";
                                    break;
                                case 2:
                                    string += "2nd";
                                    break;
                                case 3:
                                    string += "3rd";
                                    break;
                                default:
                                    string += attempt + "th";
                                    break;
                            }
                            string += " attempt.";

                            //spawn one black and one myst knight
                            spawnMob(9300036, -350, 150, cm.getPlayer().getMap());
                            spawnMob(9300037, 400, 150, cm.getPlayer().getMap());

                            cm.sendOk(string);
                            eim.setProperty("stage3attempt", attempt + 1);
                        } else {
                            //reset the combo and mass spawn monsters
                            eim.setProperty("stage3combo", "reset");
                            cm.sendOk("你已经失败了测试。请冷静下来，稍后再试。");

                            for (var i = 0; i < 6; i++) {
                                //keep getting new monsters, lest we spawn the same monster five times o.o!
                                spawnMob(9300036, randX(), 150, cm.getPlayer().getMap());
                                spawnMob(9300037, randX(), 150, cm.getPlayer().getMap());
                            }
                        }

                        eim.showWrongEffect();
                    }
                } else {
                    cm.sendOk("请确保你的尝试已经正确地放置在家臣面前，然后再和我交谈。");
                }
            }
        } else {
            cm.sendOk("请让你们的领袖和我交谈。");
        }
    }

    cm.dispose();
}

function action(mode, type, selection) {}

function makeCombo() {
    var combo = 0;

    for (var i = 0; i < 4; i++) {
        combo += (Math.floor(Math.random() * 4) * Math.pow(10, i));
    }

    return combo;
}

function getRawItems() {
    var mapItems = cm.getPlayer().getMap().getItems();
    var rawItems = [];

    var iter = mapItems.iterator();
    while (iter.hasNext()) {
        var item = iter.next();
        var id = item.getItem().getItemId();
        if (id < 4001027 || id > 4001030) {

        } else {
            rawItems.push(item);
        }
    }

    return rawItems;
}

//check the items on ground and convert into an applicable string; null if items aren't proper
function getGroundItems() {
    var itemInArea = [-1, -1, -1, -1];

    var rawItems = getRawItems();
    if (rawItems.length != 4) {
        return null;
    }

    for (var j = 0; j < rawItems.length; j++) {
        var item = rawItems[j];
        var id = item.getItem().getItemId();

        //check item location
        for (var i = 0; i < 4; i++) {
            if (cm.getPlayer().getMap().getArea(i).contains(item.getPosition())) {
                itemInArea[i] = id - 4001027;
                break;
            }
        }
    }

    //guaranteed four items that are part of the stage 3 item set by this point, check to see if each area has an item
    if (itemInArea[0] == -1 || itemInArea[1] == -1 || itemInArea[2] == -1 || itemInArea[3] == -1) {
        return null;
    }

    return ((itemInArea[0] * 1000) + (itemInArea[1] * 100) + (itemInArea[2] * 10) + itemInArea[3]);
}

function removeGroundItems() {
    var map = cm.getMap();
    var rawItems = getRawItems();
    for (var j = 0; j < rawItems.length; j++) {
        map.makeDisappearItemFromMap(rawItems[j]);
    }
}

//for mass spawn
function randX() {
    return -350 + Math.floor(Math.random() * 750);
}

function spawnMob(id, x, y, map) {
    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(id);
    map.spawnMonsterOnGroundBelow(mob, new Point(x, y));
}