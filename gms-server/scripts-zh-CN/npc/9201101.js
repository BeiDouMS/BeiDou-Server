/**
 *9201101 - T-1337
 *@author Ronan
 */

function start() {
    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
        cm.openShopNPC(9201101);
    } else {
        //cm.sendOk("新叶城的巡逻队随时待命。没有任何生物能够突破到城市里。");
        cm.sendDefault();
    }

    cm.dispose();
}