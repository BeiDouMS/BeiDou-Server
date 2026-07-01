/**
 * 拍卖行中心 — 配置驱动版
 * NPC: 9900001
 * 加新菜单项只需往 MENU_SECTIONS 加一条配置
 */

// ======================== UI 资源常量（最先定义） ========================

var changeLine = "\r\n";

// 标题横幅
var 翅膀左 = "#fUI/ChatBalloon/118/nw#";
var 翅膀中间 = "#fUI/ChatBalloon/118/n#";
var 翅膀右 = "#fUI/ChatBalloon/118/ne#";
var Logo = "#fUI/UIWindow/AdminClaim/default/1#";
var 禁止脚本 = "#fUI/UIWindow/AdminClaim/BtClaim/disabled/0#";

// 资产图标
var 正方箭头 = "#fUI/Basic/BtHide3/mouseOver/0#";

// 装饰图标
var 左修饰 = "#fItem/Etc/0427/04270001/Icon9/0#";
var 右修饰 = "#fItem/Etc/0427/04270001/Icon9/0#";
var 皇冠 = "#fUI/UIWindow/UserInfo/bossPetCrown#";
var 分割线3 = "#fUI/UIWindow/AdminClaim/default/3#";

// 标题文本（依赖上述变量）
var OldTitle = "\t\t\t\t   " + Logo + "\r\n" +
    翅膀左 + 翅膀中间.repeat(2) + "\t\t#e欢迎来到  #r萧曳  #k冒险岛#n\t\t" + 翅膀中间.repeat(2) + 翅膀右 + "\t" + changeLine + "\r\n" +
    禁止脚本;

// ======================== 菜单配置（唯一修改点） ========================

/**
 * action 类型：
 *   { type: "npc", script: "脚本名" }        -> 打开子脚本
 *   { type: "shop", shopId: 9900001 }        -> 打开商店
 *   { type: "warp", map: 910000000, portal: "out00", saveLoc: "FREE_MARKET" }
 *   { type: "warp", map: 910001000, saveOnWarp: true }
 *   { type: "msg", msg: "消息" }             -> 提示并关闭
 */
var MENU_SECTIONS = [
    {
        title: "常用功能",
        icon: 分割线3,
        items: [
            { id: 11, label: "快捷传送", action: { type: "npc",  script: "万能传送" } },
            { id: 12, label: "便捷商店", action: { type: "shop", shopId: 9900001 } },
            { id: 13, label: "超级仓库", action: { type: "npc",  script: "xy/仓库" } }
        ]
    },
    {
        title: "日常收集",
        icon: 分割线3,
        items: [
            { id: 21, label: "日常任务", action: { type: "npc", script: "每日签到" } },
            { id: 22, label: "收集系统", action: { type: "npc", script: "xy/卡片收集" } },
            { id: 23, label: "系统活动", action: { type: "msg", msg: "活动筹备中，敬请期待！" } }
        ]
    },
    {
        title: "社交兑换",
        icon: 分割线3,
        items: [
            { id: 31, label: "师徒系统", action: { type: "npc", script: "xy/mentor/师徒系统" } },
            { id: 32, label: "家族系统", action: { type: "npc", script: "xy/家族系统" } },
            { id: 33, label: "兑换中心", action: { type: "npc", script: "xy/other/物品兑换" } }
        ]
    },
    {
        title: "福利",
        icon: 分割线3,
        items: [
            { id: 41, label: "每日福利",  action: { type: "npc", script: "新人福利" } },
            { id: 44, label: "银行系统",  action: { type: "npc", script: "xy/other/银行系统" } },
            { id: 42, label: "赞助福利",  action: { type: "npc", script: "xy/vip/赞助中心" } },
            { id: 43, label: "CDKEY兑换", action: { type: "npc", script: "xy/vip/CDK_兑换" } }
        ]
    },
    {
        title: "GM功能",
        icon: 皇冠,
        gmOnly: true,
        items: [
            { id: 900, label: "GM商店",       action: { type: "shop", shopId: 9900001 } },
            { id: 901, label: "一键刷道具",   action: { type: "npc",  script: "一键刷道具" } },
            { id: 902, label: "在线玩家",     action: { type: "npc",  script: "xy/gm/在线玩家" } },
            { id: 903, label: "UI查询",       action: { type: "npc",  script: "UI查询" } },
            { id: 904, label: "发送公告",     action: { type: "npc",  script: "xy/gm/发送公告" } },
            { id: 905, label: "巡查面板",     action: { type: "npc",  script: "xy/gm/巡查面板" } },
            { id: 906, label: "召唤BOSS",     action: { type: "npc",  script: "xy/gm/召唤野外BOSS" } },
            { id: 907, label: "封禁",         action: { type: "npc",  script: "xy/gm/封禁" } },
            { id: 908, label: "物品查询",     action: { type: "npc",  script: "xy/gm/物品查询" } },
            { id: 909, label: "虚空索物",     action: { type: "npc",  script: "xy/gm/虚空索物" } },
            { id: 910, label: "任意门",       action: { type: "npc",  script: "xy/gm/任意门" } },
            { id: 911, label: "装备制作",     action: { type: "npc",  script: "xy/装备系统/v000/套装制作升级" } }
        ]
    }
];

// ======================== 引擎函数 ========================

function buildMenuText() {
    var text = OldTitle + buildHeaderInfo();
    // 传送快捷入口
    text += "\t\t" + 左修饰 + redSelect(1, "[自由市场]") + 右修饰 + "\t\t" + 左修饰 + redSelect(2, "[匠人街区]") + 右修饰 + changeLine.repeat(2);

    var i, j, section, items;
    for (i = 0; i < MENU_SECTIONS.length; i++) {
        section = MENU_SECTIONS[i];
        if (section.gmOnly && !cm.getPlayer().isGM()) {
            continue;
        }
        text += "   " + (section.icon || 分割线3) + "#n";
        items = section.items;
        for (j = 0; j < items.length; j++) {
            text += generalSelect(items[j].id, items[j].label);
            if (j < items.length - 1) {
                text += "\t";
            }
        }
        text += "\\t" + changeLine.repeat(2);
    }
    return text;
}

// 数字格式化工具（Intl可能在某些GraalJS版本不可用）
function fmtNum(n) {
    try {
        return new Intl.NumberFormat().format(n);
    } catch (e) {
        return String(n);
    }
}

function buildHeaderInfo() {
    var cashShop = cm.getPlayer().getCashShop();
    var onlineMs = cm.getOnlineTime();
    var timeStr;
    if (onlineMs < 3600) {
        timeStr = "在线时间：#e#r" + Math.floor(onlineMs / 60) + "#k#n 分钟";
    } else {
        var hour = Math.floor(onlineMs / 3600);
        var min = Math.floor((onlineMs % 3600) / 60);
        timeStr = "在线时间：#e#r" + hour + "#k#n 小时 #e#r" + min + "#k#n 分钟";
    }
    var info = "";
    info += "\t" + 正方箭头 + " 点券：" + fmtNum(cashShop.getCash(1)) + "\t\t\t\t" + timeStr + changeLine;
    info += "\t" + 正方箭头 + " 抵用：" + fmtNum(cashShop.getCash(2)) + "\t\t\t\t" + 正方箭头 + " 信用：" + fmtNum(cashShop.getCash(4)) + changeLine;
    info += "\t" + 正方箭头 + " 金币：" + fmtNum(cm.getPlayer().getMeso()) + changeLine;
    return info;
}

function doSelect(sel) {
    // 传送快捷入口
    if (sel === 1) {
        cm.getPlayer().saveLocation("FREE_MARKET");
        cm.warp(910000000, "out00");
        return;
    }
    if (sel === 2) {
        cm.getPlayer().saveLocationOnWarp();
        cm.getPlayer().dropMessage(6, "[传送中心]：[" + cm.getPlayer().getName() + "玩家] [线路-" + cm.getPlayer().getClient().getChannel() + "] 传送至 匠人街");
        cm.warp(910001000);
        cm.dispose();
        return;
    }

    var i, j, section, items;
    for (i = 0; i < MENU_SECTIONS.length; i++) {
        section = MENU_SECTIONS[i];
        items = section.items;
        for (j = 0; j < items.length; j++) {
            if (items[j].id === sel) {
                execute(items[j].action);
                return;
            }
        }
    }
    cm.sendOk("该功能暂不支持，敬请期待！");
    cm.dispose();
}

function execute(a) {
    if (a.type === "npc") {
        cm.dispose();
        cm.openNpc(9900001, a.script);
    } else if (a.type === "shop") {
        cm.dispose();
        cm.openShopNPC(a.shopId);
    } else if (a.type === "warp") {
        if (a.saveLoc) {
            cm.getPlayer().saveLocation(a.saveLoc);
        }
        if (a.saveOnWarp) {
            cm.getPlayer().saveLocationOnWarp();
            cm.getPlayer().dropMessage(6, "[传送中心]：[" + cm.getPlayer().getName() + "玩家] [线路-" + cm.getPlayer().getClient().getChannel() + "] 传送至 匠人街");
        }
        cm.warp(a.map, a.portal || "");
    } else if (a.type === "msg") {
        cm.sendOk(a.msg);
        cm.dispose();
    }
}

// ======================== 脚本入口 ========================

var status = -1;

function start() {
    status = -1;
    action(1, 0, 0);
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
        cm.sendSimple(buildMenuText());
    } else if (status === 1) {
        doSelect(selection);
    } else {
        cm.dispose();
    }
}

// ======================== UI 标记函数 ========================

function redSelect(idNum, text) {
    return "#L" + idNum + "##r" + text + "#k#n#l";
}

function generalSelect(idNum, text) {
    return "#L" + idNum + "#" + text + "#l";
}

// ======================== 保留变量（供后续扩展） ========================
var 皇冠白 = "#fUI/GuildMark/Mark/Etc/00009004/15#";
var 完成 = "#fUI/UIWindow/Quest/Tab/enabled/2#";
var 圆形 = "#fUI/UIWindow/Quest/icon3/6#";
var 感叹号 = "#fUI/UIWindow/Quest/icon0#";
var 粉心 = "#fEffect/CharacterEff/1112903/0/1#";
var 红心 = "#fEffect/CharacterEff/1082229/0/0#";
var 火箭 = "#fUI/GuildMark/Mark/Etc/00009022/12#";
var 奖励 = "#fUI/CashShop/CSDiscount/bonus#";
var line = "#fUI/CashShop/CSDiscount/Line#";
var 分割线1 = "#fUI/UIWindow/AdminClaim/default/2#";
var 分割线2 = "__________________________________________________";
var 分割线4 = "#fUI/UIWindow/AdminClaim/default/4#";
var 金枫叶 = "#fMap/MapHelper/weather/maple/2#";
var 草莓5 = "#fUI/GuildMark/Mark/Plant/00003000/8#";
