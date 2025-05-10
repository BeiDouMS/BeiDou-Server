/**
 * 骑士团-女皇之路（MapID：130030001）
 * 触发召唤NPC利林跟随
 * @param pi
 * @returns {boolean}
 */
function enter(pi) {
    pi.spawnGuide();// 生成（召唤）新手引导NPC
    pi.talkGuide("欢迎来到北斗世界！我是利琳。我会负责引导你，直到你达到10级并成为见习骑士。双击我获取更多信息！");// 让引导NPC对玩家发送消息
    pi.blockPortal();// 阻止玩家通过传送门（防止玩家提前离开新手引导区域）
    return true;// 返回 `true`，表示事件执行成功
}