/**
 *9201101 - T-1337
 *@author Ronan
 */

function start() {
    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
        cm.openShopNPC(9201101);
    } else {
        //cm.sendOk("The patrol in New Leaf City is always ready. No creatures are able to break through to the city.");
        cm.sendDefault();
    }

    cm.dispose();
}
