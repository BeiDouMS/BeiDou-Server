/**北斗脚本

北斗之星-2430033

---By hanmburger*/
/**
 * 名称：北斗指导书奖励脚本
 * 功能：用于使用指定物品进行兑换的脚本，采用JSON配置奖品，NextLevel构造脚本，具有配置清晰便捷、代码逻辑清晰的特点。
 * 		支持配置的奖励有 金币、经验、点券、抵用券、信用点 和 有效物品
 * 		支持输入整数按份领取奖励列表
 * 作者：Magical-H (https://github.com/Magical-H)
 * 版本：1.0
 * 日期：2025-06-07
 */

/* 可配置区域 */
var itemCountMax = 100;	//单次兑换份数上限
var config = {
	haveitem: {id: 2430033,qty: 1, unit:"本"},	//需求物品ID和数量
	reward: [//奖励列表
		// id解释：0：金币;  1：点券;  2：抵用券;  4、信用点; 5、经验值;  id ≥ 1_000_000 的为有效物品ID，自动区分，你只需要操心客户端有没有填入的物品即可。
		//{id：物品ID，qty：数量},
		{id: 0,qty: 1000},	//金币
		{id: 5,qty: 1000},	//经验值
		{id: 1,qty: 10000},	//点券
		{id: 2000016,qty: 10},
		{id: 2000018,qty: 10},
		{id: 2050004,qty: 1},
		{id: 4006001,qty: 2},
		{id: 5451000,qty: 1}
	]
};

/* 无需配置区域 */
var status = -1;
var itemQty = 0;		//持有硬币数量
var itemCount = 0;		//最大可兑换份数
var itemDeduct = 0;		//总扣除硬币数量
var itemIcoName = '';	//硬币图标与名称（客户端代码）

// 定义物品显示模板的映射表（ES6对象字面量优化）
const ITEM_TEMPLATES = {
	0: '#fUI/Basic.img/BtCoin/normal/0#   #fUI/UIWindow.img/QuestIcon/7/0#',	//金币
	1: '#fUI/CashShop.img/CashItem/0#   #e#b点券#k#n',
	2: '#fUI/CashShop.img/CashItem/0#   #e#b抵用券#k#n',
	4: '#fUI/CashShop.img/CashItem/0#   #e#b信用点#k#n',
	5: '#fUI/UIWindow.img/AriantMatch/characterIcon/2#   #fUI/UIWindow.img/QuestIcon/8/0#'	//经验值
};

/* 函数方法区域 */
function start() {
  status = -1;
  action(1, 0, 0);
}

function action(mode, type, selection) {
	levelmain();
}

function level() {
	im.dispose();
}

function levelnull() {
	level();
}

function leveldispose() {
	level();
}

function levelmain() {
	if (config.haveitem.qty < 1) config.haveitem.qty = 1;	//至少需要扣除1个道具

		itemIcoName = getCoinInfo();
	let msg = `\r\n传说只要能集齐 #r${config.haveitem.qty + config.haveitem.unit}#k ${itemIcoName} `;
		itemQty = im.itemQuantity(config.haveitem.id);				//获取硬币持有数
		itemCount = parseInt(itemQty / config.haveitem.qty);		//计算已持有硬币可兑换的最大份数
		CountMax = Math.min(itemCount,itemCountMax);
	if (itemQty >= config.haveitem.qty) {
		msg += `即可获得以下奖励：\r\n\r\n#fUI/CashShop.img/CSDiscount/bonus#\r\n`;
		msg += getRewardList() + `\r\n\r\n\r\n你当前最多可以兑换 #b${itemCount} 份 #k奖励，你想兑换多少份奖励？\r\n目前单次兑换上限： #r${itemCountMax} 份#k\r\n `;
		im.getInputNumberLevel("ConfirmReceipt", msg, CountMax, 1,CountMax);
	} else {
		msg += "即可获得神秘奖励！\r\n快去收集吧！";
		im.sendOkLevel("",msg);
	}
}

function levelConfirmReceipt(count) {
	if (count <= 0 || count > itemCount) {
		im.sendOkLevel("","输入的份数不能 ≤0 且 不能超过最大份数 " + itemCount);
	} else {
			itemDeduct = count * config.haveitem.qty;	//计算硬币扣除数量
			itemCount = count;							//更新硬币兑换份数
		let msg = `即将兑换 #r${count} 份#k 以下奖励：\r\n\r\n#fUI/CashShop.img/CSDiscount/bonus#\r\n`;
			msg += getRewardList(count) + `\r\n\r\n\r\n是否使用  ${itemIcoName} × #r${itemDeduct} ${config.haveitem.unit}#k 进行兑换？`;
		im.sendYesNoLevel("main","giveRewardItems",msg);
	}
}

function levelgiveRewardItems() {
	im.sendOkLevel("",giveRewardItems(itemCount));
}
/**
 * 生成奖励物品的显示列表
 * @param {Number} count - 包含奖励数据的配置对象
 * @returns {string} 格式化后的奖励列表字符串，每项用换行符分隔
 */
function getRewardList(count) {
	return config.reward.map(obj => {
		let { id, qty } = obj;// 通过解构赋值获取当前物品属性
		let itemshow = ITEM_TEMPLATES[id] ?? '';// 根据物品ID获取基础显示模板（使用空值合并运算符??）
		if (count > 1) qty = `${qty}#k × ${count}份 = #b${qty * count}#k`;
		// 处理不同物品类型的显示逻辑
		if (id >= 1_000_000) {  // 有效的物品ID≥7位数，使用数字分隔符提高可读性
			itemshow = `#i${id}#   #e#b#t${id}##k#n × #r${qty}#k`;
		} else if (itemshow) {// 已知物品追加数量显示
			itemshow += ` × #r${qty}#k`;
		} else {// 未知物品显示错误提示
			itemshow = `#fUI/UIWindow.img/KeyConfig/BtHelp/mouseOver/0# #e#r未知物品ID：[#k ${id} #r]#k#n`;
		}
		return `#fUI/CashShop.img/CSDiscount/arrow# ${itemshow}`;// 为每项添加统一前缀并返回
	}).join('\r\n');  // 用回车换行符连接所有项
}

/**
 * 发放奖励道具（支持多份兑换）
 * @param {number} [count=1] - 兑换份数（默认为1）
 * @returns {string} 发放结果信息（失败时返回无法兑换的物品列表）
 */
function giveRewardItems(count) {
	if (count <= 0 || count > itemCount) {
		return "输入的份数不能 ≤0 且 不能超过最大份数 " + itemCount;
	}
	// 再次验证兑换硬币持有数量
	itemQty = im.itemQuantity(config.haveitem.id);	//获取背包持有量
	itemDeduct = count * config.haveitem.qty;		//计算硬币扣除数量
	if (itemQty < itemDeduct) {	//验证硬币数量是否足够扣除
		return `当前持有的\r\n${itemIcoName} × #r${itemQty + config.haveitem.unit}#k \r\n不足以兑换 #r${count} 份#k \r\n所需的数量 #r${itemDeduct + config.haveitem.unit}#k`;
	}
	// 验证普通物品
	const failedItems = [];
	const normalItems = config.reward.filter(obj => obj.id >= 1_000_000);

	for (const {id, qty} of normalItems) {
		const totalQty = qty * count;
		if (!im.canHold(id, totalQty)) {
			failedItems.push(`#fUI/UIWindow.img/FadeYesNo/BtCancel/mouseOver/0# #i${id}#   #b#t${id}##k × #r${totalQty}#k`);
		}
	}

	if (failedItems.length > 0) {
		return ` 背包空间不足，无法兑换以下物品：\r\n\r\n${failedItems.join('\r\n')}`;
	} else {
		im.gainItem(config.haveitem.id,-itemDeduct);	//扣除对应份数的硬币数量
	}

	// 实际发放所有物品
	const successItems = [];
	for (const {id, qty} of config.reward) {
		const totalQty = qty * count;
		let succitemshow;

		if (id >= 1_000_000) {
			im.gainItem(id, totalQty);
			succitemshow = `#i${id}#   #b#t${id}##k × #r${totalQty}#k`;
		} else {
			switch(id) {
				case 0:
					im.gainMeso(totalQty);
					succitemshow = `${ITEM_TEMPLATES[id]} × #r${totalQty}#k`;
					break;
				case 1:
				case 2:
				case 4:
					im.getPlayer().getCashShop().gainCash(id, totalQty);
					succitemshow =`${ITEM_TEMPLATES[id]} × #r${totalQty}#k`;
					break;
				case 5:
					im.gainExp(totalQty);
					succitemshow =`${ITEM_TEMPLATES[id]} × #r${totalQty}#k`;
					break;
			}
		}
		if (succitemshow) {
			successItems.push(`#fUI/Basic.img/CheckBox/1# ${succitemshow}`);
		}
	}

	return `#fUI/UIWindow.img/QuestIcon/4/0#\r\n\r\n${successItems.join('\r\n')}`;
}

/**
 * 返回硬币图标与名称
 */
function getCoinInfo() {
	return `#i${config.haveitem.id}# #e#b#t${config.haveitem.id}##k#n`;
}