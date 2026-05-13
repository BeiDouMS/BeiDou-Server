/**
 * @description 学习技能
 */
var OldTitle = "#eBeiDou技能学习中心#n\r\n";
var status = -1;
var lastSkillInfo = null;
var resultType = "";

function start() {
    action(1, 0, 0)
}

function action(mode, type, selection) {
    if (mode === 1) {
        status++;
    } else if (mode === -1) {
        status--;
    } else {
        cm.dispose();
        return;
    }

    if (status === 0) {
        showSkillList();
    } else if (status === 1) {
        handleSkillSelection(selection);
    } else if (status === 2) {
        handleResultSelection(selection);
    }
}

function showSkillList() {
    var player = cm.getPlayer();
    
    // 检查玩家技能状态
    var hasDoubleJump = player.getSkillLevel(4111006) > 0;
    var hasTeleport = player.getSkillLevel(2101002) > 0;
    
    var text = OldTitle;
    text += "点击技能自动完成：#n\r\n\r\n";
    text += "1. 学习技能（未学会时）\r\n";
    text += "2. 绑定到指定按键\r\n\r\n";
    text += "#b════════════════#k\r\n\r\n";
    
    // 根据技能状态显示不同的文本
    if (hasDoubleJump) {
        text += "#L0##b#s4111006# #q4111006##k (已学会，重新绑定#e-#n键)#l\r\n\r\n";
    } else {
        text += "#L0##b#s4111006# #q4111006##k (学习并绑定#e-#n键)#l\r\n\r\n";
    }
    
    if (hasTeleport) {
        text += "#L1##b#s2101002# #q2101002##k (已学会，重新绑定#e+#n键)#l\r\n\r\n";
    } else {
        text += "#L1##b#s2101002# #q2101002##k (学习并绑定#e+#n键)#l\r\n\r\n";
    }
    
    text += "#L2#取消#l";
    
    cm.sendSimple(text);
}

function handleSkillSelection(selection) {
    var skillId, keyCode, skillName, keyName, keySymbol;
    
    switch (selection) {
        case 0:  // 二段跳
            skillId = 4111006;
            keyCode = 12;  // -键
            skillName = "#q4111006#";
            keyName = "#e-#n键";
            keySymbol = "#e-#n";
            break;
        case 1:  // 快速移动
            skillId = 2101002;
            keyCode = 13;  // +键
            skillName = "#q2101002#";
            keyName = "#e+#n键";
            keySymbol = "#e+#n";
            break;
        case 2:  // 取消
            cm.sendOk("#b已取消。#k");
            cm.dispose();
            return;
    }
    
    // 保存技能信息
    lastSkillInfo = {
        skillId: skillId,
        keyCode: keyCode,
        skillName: skillName,
        keyName: keyName,
        keySymbol: keySymbol
    };
    
    // 处理技能学习和绑定
    var result = processSkill(skillId, keyCode, skillName, keyName, keySymbol);
    
    // 记录结果类型
    resultType = result.success ? "success" : "fail";
    
    // 显示结果
    showResult(result);
}

function showResult(result) {
    var text = OldTitle;
    text += "#b════════════════#k\r\n";
    
    if (result.success) {
        text += "#e操作结果#n\r\n";
        text += "#b════════════════#k\r\n\r\n";
        text += result.message;
        text += "\r\n\r\n#b════════════════#k\r\n\r\n";
        text += "#L0#再学其他技能#l\r\n";
        text += "#L1#完成#l";
    } else {
        text += "#r操作失败#n\r\n";
        text += "#b════════════════#k\r\n\r\n";
        text += result.message;
        text += "\r\n\r\n#b════════════════#k\r\n\r\n";
        text += "#L0#再试一次#l\r\n";
        text += "#L1#返回#l\r\n";
        text += "#L2#取消#l";
    }
    
    cm.sendSimple(text);
}

function handleResultSelection(selection) {
    if (resultType === "success") {
        if (selection === 0) {
            // 继续学习其他技能
            status = 0;
            showSkillList();
        } else if (selection === 1) {
            // 完成并关闭窗口
            cm.sendOk("#b操作完成。#k");
            cm.dispose();
        }
    } else if (resultType === "fail") {
        if (selection === 0) {
            // 重新尝试
            if (lastSkillInfo !== null) {
                var result = processSkill(
                    lastSkillInfo.skillId,
                    lastSkillInfo.keyCode,
                    lastSkillInfo.skillName,
                    lastSkillInfo.keyName,
                    lastSkillInfo.keySymbol
                );
                resultType = result.success ? "success" : "fail";
                showResult(result);
            } else {
                status = 0;
                showSkillList();
            }
        } else if (selection === 1) {
            // 返回技能列表
            status = 0;
            showSkillList();
        } else if (selection === 2) {
            // 取消
            cm.sendOk("#b已取消。#k");
            cm.dispose();
        }
    }
}

// 处理技能学习和绑定的主函数
function processSkill(skillId, keyCode, skillName, keyName, keySymbol) {
    var player = cm.getPlayer();
    var messages = [];
    var success = true;
    var alreadyLearned = false;
    
    // 检查是否已学习技能
    var hasSkill = player.getSkillLevel(skillId) > 0;
    
    if (!hasSkill) {
        // 学习技能 - 直接学习，跳过职业检查
        var learnResult = learnSkill(skillId, skillName);
        if (learnResult.success) {
            messages.push(learnResult.message);
        } else {
            messages.push("#r学习失败：" + learnResult.message + "#k");
            success = false;
        }
    } else {
        messages.push("#g已学会" + skillName + "。#k");
        alreadyLearned = true;
    }
    
    // 绑定技能
    if (success) {
        var bindResult = bindSkill(skillId, skillName, keyCode, keyName, keySymbol, alreadyLearned);
        if (bindResult.success) {
            messages.push(bindResult.message);
        } else {
            messages.push("#r绑定失败：" + bindResult.message + "#k");
            success = false;
        }
    }
    
    return {
        success: success,
        message: messages.join("\r\n\r\n")
    };
}

// 学习技能的函数 - 修改版，跳过职业检查
function learnSkill(skillId, skillName) {
    var player = cm.getPlayer();
    
    if (player.getSkillLevel(skillId) > 0) {
        return {success: true, message: "#g已学会" + skillName + "。#k"};
    }
    
    try {
        var SkillFactory = Java.type('org.gms.client.SkillFactory');
        var skill = SkillFactory.getSkill(skillId);
        
        if (skill == null) {
            return {success: false, message: "技能不存在"};
        }
        
        // 获取技能最大等级
        var maxLevel = 20;
        try {
            maxLevel = skill.getMaxLevel();
            if (maxLevel <= 0) {
                maxLevel = 20;
            }
        } catch (e) {
            maxLevel = 20;
        }
        
        // 直接学习技能，不检查职业限制
        player.changeSkillLevel(skill, maxLevel, maxLevel, -1);
        
        return {
            success: true, 
            message: "#g学会" + skillName + "#k\r\n" +
                     "等级：" + maxLevel + "/" + maxLevel
        };
        
    } catch (e) {
        return {success: false, message: "学习出错：" + e.toString()};
    }
}

// 绑定技能的函数
function bindSkill(skillId, skillName, keyCode, keyName, keySymbol, alreadyLearned) {
    var player = cm.getPlayer();
    
    if (player.getSkillLevel(skillId) <= 0) {
        return {success: false, message: "还未学习此技能"};
    }
    
    try {
        var KeyBinding = Java.type('org.gms.client.keybind.KeyBinding');
        var javaKeyBinding = new KeyBinding(1, skillId);
        
        player.changeKeybinding(keyCode, javaKeyBinding);
        
        var message = "";
        if (alreadyLearned) {
            message = "#g重新绑定" + skillName + "到" + keyName + "#k";
        } else {
            message = "#g绑定" + skillName + "到" + keyName + "#k";
        }
        
        message += "\r\n按键：" + keySymbol + "键";
        message += "\r\n#e注意：#n换线后才能看到键位变化";
        
        return {success: true, message: message};
        
    } catch (e1) {
        try {
            var KeyBinding = Java.type('org.gms.client.keybind.KeyBinding');
            var javaKeyBinding = new KeyBinding(1, skillId);
            
            if (typeof player.setKeybinding === 'function') {
                player.setKeybinding(keyCode, javaKeyBinding);
            } else if (typeof player.setKeyBinding === 'function') {
                player.setKeyBinding(keyCode, javaKeyBinding);
            } else if (typeof player.updateKeybinding === 'function') {
                player.updateKeybinding(keyCode, javaKeyBinding);
            } else {
                return {success: false, message: "不支持绑定功能"};
            }
            
            var message = "";
            if (alreadyLearned) {
                message = "#g重新绑定" + skillName + "到" + keyName + "#k";
            } else {
                message = "#g绑定" + skillName + "到" + keyName + "#k";
            }
            
            message += "\r\n按键：" + keySymbol + "键";
            message += "\r\n#e注意：#n换线后才能看到键位变化";
            
            return {success: true, message: message};
            
        } catch (e2) {
            return {success: false, message: "绑定出错：" + e2.toString()};
        }
    }
}