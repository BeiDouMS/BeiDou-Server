/**
 * 功能：按物品分类查找爆率
 * 整合版（每页50个，显示ID，无子分类，含职业技能书）
 * 版本：1.0
 * 作者：Jason
 * 时间：2026年2月13日
 */

// 导入Java类
var ItemInformationProvider = Java.type('org.gms.server.ItemInformationProvider');
var MonsterInformationProvider = Java.type('org.gms.server.life.MonsterInformationProvider');
var QuestInfo = Java.type('org.gms.server.quest.Quest');
var DatabaseConnection = Java.type('org.gms.util.DatabaseConnection');

// 常量定义
const ITEMS_PER_PAGE = 50;

// 全局变量
var currentCategory = "";
var categoryItems = [];
var currentPage = 0;
var totalResults = 0;
var currentMainCategory = 0; // 记录主分类ID（用于返回）

// 主分类ID范围（合并后的区间）
var categoryRanges = {
    1: { name: "战士武器", ranges: [[1302000,1302999], [1402000,1402999], [1312000,1312999], [1412000,1412999], [1322000,1322999], [1422000,1422999], [1432000,1432999], [1442000,1442999]] },
    2: { name: "法师武器", ranges: [[1372000,1372999], [1382000,1382999]] },
    3: { name: "弓箭手武器", ranges: [[1452000,1452999], [1462000,1462999]] },
    4: { name: "飞侠武器", ranges: [[1332000,1332999], [1472000,1472999], [1342000,1342999]] },
    5: { name: "海盗武器", ranges: [[1482000,1482999], [1492000,1492999]] },
    6: { name: "防具类", ranges: [[1002000,1003999], [1042000,1042999], [1052000,1052999], [1062000,1062999], [1072000,1072999], [1082000,1082999], [1092000,1092999], [1102000,1102999]] },
    7: { name: "饰品类", ranges: [[1112000,1112999], [1122000,1122999], [1132000,1132999], [1152000,1152999], [1032000,1032999], [1022000,1022999], [1012000,1012999]] },
    8: { name: "消耗品", ranges: [[2000000,2999999]] },
    9: { name: "卷轴/强化", ranges: [[2040000,2049999]] },
    10: { name: "其他物品", ranges: [[4000000,4039999]] },
    11: { name: "职业技能书", ranges: [[2280000,2299999]] } // 新增：技能书(228)和能手册(229)
};

function start() {
    levelStart();
}

/**
 * 主菜单
 */
function levelStart() {
    var text = "#e#b请选择要查找的物品分类：#k#n\r\n\r\n";
    text += "#L1##r战士武器#k#l\r\n";
    text += "#L2##r法师武器#k#l\r\n";
    text += "#L3##r弓箭手武器#k#l\r\n";
    text += "#L4##r飞侠武器#k#l\r\n";
    text += "#L5##r海盗武器#k#l\r\n";
    text += "#L6##r防具类#k#l\r\n";
    text += "#L7##r饰品类#k#l\r\n";
    text += "#L8##r消耗品#k#l\r\n";
    text += "#L9##r卷轴/强化#k#l\r\n";
    text += "#L10##r其他物品#k#l\r\n";
    text += "#L11##r职业技能书#k#l\r\n";
    text += "#L12##r关闭#k#l";
    
    cm.sendNextSelectLevel('handleMainSelection', text, 2);
}

/**
 * 处理主菜单选择
 */
function levelhandleMainSelection(selection) {
    var sel = parseInt(selection);
    if (sel >= 1 && sel <= 11) {
        currentMainCategory = sel;
        loadCategoryItems(sel);
    } else if (sel == 12) {
        cm.dispose();
    } else {
        levelStart();
    }
}

// ============ 加载物品列表 ============
function loadCategoryItems(categoryId) {
    var category = categoryRanges[categoryId];
    currentCategory = category.name;
    categoryItems = [];
    currentPage = 0;
    
    try {
        var con = DatabaseConnection.getConnection();
        // 构建动态SQL：用多个 BETWEEN OR 连接
        var sql = "SELECT itemid FROM drop_data WHERE ";
        var conditions = [];
        var params = [];
        for (var i = 0; i < category.ranges.length; i++) {
            conditions.push("(itemid BETWEEN ? AND ?)");
            params.push(category.ranges[i][0], category.ranges[i][1]);
        }
        sql += conditions.join(" OR ");
        sql += " GROUP BY itemid LIMIT 200"; // 最多取200个物品
        
        var ps = con.prepareStatement(sql);
        for (var i = 0; i < params.length; i++) {
            ps.setInt(i+1, params[i]);
        }
        var rs = ps.executeQuery();
        
        while (rs.next()) {
            var itemId = rs.getInt("itemid");
            var itemName = ItemInformationProvider.getInstance().getName(itemId);
            if (itemName != null && itemName != "MISSINGNO") {
                categoryItems.push({
                    id: itemId,
                    name: itemName
                });
            }
        }
        
        rs.close();
        ps.close();
        con.close();
        
        if (categoryItems.length == 0) {
            cm.sendOkLevel('levelStart', "#r该分类下没有找到有掉落记录的物品。#k", 2);
            return;
        }
        
        // 按名称排序
        categoryItems.sort(function(a, b) {
            return a.name.localeCompare(b.name);
        });
        
        totalResults = categoryItems.length;
        showCategoryItems();
        
    } catch (e) {
        cm.sendOkLevel('levelStart', "#r加载物品列表时发生错误：#k" + e, 2);
    }
}

/**
 * 显示物品列表（分页，显示ID）
 */
function showCategoryItems() {
    var start = currentPage * ITEMS_PER_PAGE;
    var end = Math.min(start + ITEMS_PER_PAGE, totalResults);
    
    var text = "#e#b" + currentCategory + "#k#n 物品列表（共 " + totalResults + " 个）：\r\n\r\n";
    
    for (var i = start; i < end; i++) {
        var item = categoryItems[i];
        text += "#L" + item.id + "##v" + item.id + "# #b" + item.name + "#k (ID: " + item.id + ")#l\r\n";
    }
    
    text += "\r\n";
    
    // 分页控制（使用大正数避免冲突）
    if (currentPage > 0) {
        text += "#L9000001#<< 上一页#l\r\n";
    }
    if (end < totalResults) {
        text += "#L9000002#下一页 >>#l\r\n";
    }
    
    if (totalResults > ITEMS_PER_PAGE) {
        text += "\r\n当前第 " + (currentPage + 1) + " 页，共 " + Math.ceil(totalResults / ITEMS_PER_PAGE) + " 页\r\n";
    }
    
    text += "\r\n#L9000003#返回上级菜单#l\r\n";
    text += "#L9000004#返回主菜单#l";
    
    cm.sendNextSelectLevel('handleItemSelection', text, 2);
}

/**
 * 处理物品列表选择
 */
function levelhandleItemSelection(selection) {
    var sel = parseInt(selection);
    
    if (sel == 9000001) {
        currentPage--;
        showCategoryItems();
    } else if (sel == 9000002) {
        currentPage++;
        showCategoryItems();
    } else if (sel == 9000003) {
        // 返回上级菜单：回到主菜单（因为没有子分类）
        levelStart();
    } else if (sel == 9000004) {
        levelStart();
    } else if (sel > 0) {
        // 物品ID处理
        var itemId = sel;
        var itemName = ItemInformationProvider.getInstance().getName(itemId);
        if (itemName) {
            showItemDropInfo(itemId, itemName);
        } else {
            cm.sendOkLevel('showCategoryItems', "#r物品信息获取失败。#k", 2);
        }
    } else {
        cm.sendOkLevel('showCategoryItems', "#r选择无效。#k", 2);
    }
}

/**
 * 显示物品掉落信息
 */
function showItemDropInfo(itemId, itemName) {
    var msg = "#e#b" + itemName + "#k#n (ID: " + itemId + ") 的掉落信息：\r\n\r\n";
    
    try {
        var con = DatabaseConnection.getConnection();
        var ps = con.prepareStatement("SELECT dropperid, chance, questid FROM drop_data WHERE itemid = ? ORDER BY chance DESC LIMIT 30");
        ps.setInt(1, itemId);
        var rs = ps.executeQuery();
        
        var hasResults = false;
        var dropList = [];
        
        while (rs.next()) {
            hasResults = true;
            var mobId = rs.getInt("dropperid");
            var chance = rs.getInt("chance") / 10000;
            var questId = rs.getInt("questid");
            
            dropList.push({
                mobId: mobId,
                chance: chance,
                questId: questId
            });
        }
        
        rs.close();
        ps.close();
        con.close();
        
        if (!hasResults) {
            msg += "#r没有找到掉落该物品的怪物。#k";
        } else {
            for (var i = 0; i < dropList.length; i++) {
                var drop = dropList[i];
                var mobName = MonsterInformationProvider.getInstance().getMobNameFromId(drop.mobId);
                
                if (mobName != null) {
                    msg += "#b" + mobName + "#k (ID: " + drop.mobId + ")\r\n";
                    msg += "  #g爆率: " + drop.chance.toFixed(4) + "%#k";
                    
                    var playerRate = drop.chance * cm.getPlayer().getDropRate() * cm.getPlayer().getFamilyDrop();
                    if (Math.abs(playerRate - drop.chance) > 0.0001) {
                        msg += " → #d" + playerRate.toFixed(4) + "%#k";
                    }
                    
                    if (drop.questId > 0) {
                        msg += " #r[任务道具]#k";
                    }
                    msg += "\r\n\r\n";
                }
            }
        }
        
        msg += "\r\n#L9000003#返回物品列表#l   #L9000004#返回主菜单#l";
        
        cm.sendNextSelectLevel('handleDropInfo', msg, 2);
        
    } catch (e) {
        cm.sendOkLevel('showCategoryItems', "#r查询过程中发生错误：#k" + e, 2);
    }
}

/**
 * 处理爆率详情页的选择
 */
function levelhandleDropInfo(selection) {
    var sel = parseInt(selection);
    if (sel == 9000003) {
        showCategoryItems();
    } else if (sel == 9000004) {
        levelStart();
    }
}

// 错误处理返回
function levelshowCategoryItems() {
    showCategoryItems();
}