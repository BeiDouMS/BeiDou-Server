/**
 * 功能：展示当前地图存活的怪物种类以及物品爆率
 * 作者：Magical-H (https://github.com/Magical-H)
 * 版本：1.0
 * 日期：2024-12-02
 */
var MonsterInformationProvider;
var ItemInformationProvider;
var QuestInfo;

var MapObj;								 //地图对象
var List_Mob_All;				   //所有怪物列表
var List_Mob_Boss;				  //BOSS列表
var List_Mob;						   //普通怪物列表
var namelength = 0;

function start(){
    if(MapObj == null) {		//首次打开进行初始化。
        MonsterInformationProvider = Java.type('org.gms.server.life.MonsterInformationProvider');//导入 怪物信息 类
        ItemInformationProvider = Java.type('org.gms.server.ItemInformationProvider');//导入 物品信息 类
        QuestInfo = Java.type('org.gms.server.quest.Quest');//导入 任务 类
        MapObj = cm.getMap();
        List_Mob_All = MapObj.getAllMonsters(); //获取当前地图存活的怪物数量，由于未找到获取当前地图固定怪物列表方法，故用此方法代替。
        //将怪物种类去重并按照Boss和普通怪物区分开
        [List_Mob, List_Mob_Boss] = Object.values(List_Mob_All.reduce((acc, mob) => (acc.ids.has(mob.getId()) || (acc.ids.add(mob.getId()), mob.isBoss() ? acc.bosses : acc.mobs).push(mob), acc), { ids: new Set(), mobs: [], bosses: [] })).slice(-2);
    }
    levelmain();
}

function leveldispose() {
    cm.dispose();
}

/**
 * 当部分cm.sendLevel方法没有指定下一个跳转方法时会自动跳入null，也就是这里。
 */
function levelnull() {
    cm.dispose();
}

/**
 * 这里是第一层对话框，用于展示当前地图存活的怪物种类。
 */
function levelmain() {
    if(List_Mob_All.length == 0) {
        cm.sendOkLevel('dispose', '当前地图没有存活的怪物，请等待怪物刷新后再进行查询。', 2);
    } else {
        var Msg_Select = '当前地图#b#e存活#k#n怪物列表一览，点击可查看掉落列表：\r\n'; //怪物选择展示消息
            Msg_Select += '#d' + '\r\n'.padStart(28,'——') + '#k';
        if(List_Mob_Boss.length > 0) {
            Msg_Select += `#e#rBOSS#k#n：${List_Mob_Boss.length} 种\r\n`;
            Msg_Select += getSelecttext(List_Mob_Boss);
            if(List_Mob.length > 0) Msg_Select += '#d' + '\r\n'.padStart(28,'——') + '#k';
        }
        if(List_Mob.length > 0) {
            Msg_Select += `普通怪物：${List_Mob.length} 种\r\n`;
            Msg_Select += getSelecttext(List_Mob);
        }
        cm.sendNextSelectLevel('ShowDropList', Msg_Select,2);
    }
}

/**
 * 格式化输出当前地图存在的怪物列表选项。
 * @param moblist
 * @returns {string}
 */
function getSelecttext(moblist) {
    let size = moblist.reduce((max, obj) => Math.max(max, obj.getName().length), 0);  //取最长怪物名称长度
    namelength = namelength > size ? namelength : size;
    return moblist.map(obj => {
        let id = obj.getId();
        let select = '#fUI/UIWindow.img/UserList/Friend/icon04# ';
        let name = !obj.getName() || obj.getName() == 'MISSINGNO' ? `#o${id}#` : obj.getName();     //优先以服务器怪物名称为准，没有的话就显示客户端的
            name = select + name.padEnd(namelength,'\t');
        return `#L${id}#${getMobImage(obj)}\r\n#${(obj.isBoss() ? 'r' : 'b') + name}#k\t[ Lv.${getLevelImage(obj.getLevel())} ] #l`
    }).join('\r\n\r\n') + '\r\n\r\n';
}

/**
 * 格式化输出指定怪物的掉落物品列表
 * @param mobId
 */
function levelShowDropList(mobId) {
    const mob = List_Mob_All.find(mob => mob.getId() == mobId);		 //根据怪物ID获取已缓存的怪物对象
    const table = {
        '物品名称' : 0,
        '基础掉率' : 0,
        '你的掉率' : 0,
    }
    let msgtext = `当前查询的怪物ID [ ${mobId} ] 不存在。`;

    if(mob != null) {
        let player = cm.getPlayer();
        let dropall = MonsterInformationProvider.getInstance().retrieveDrop(mobId);										 //根据怪物ID获取掉落物品列表
        let CountItems = dropall.size();																				//取掉落物品总数量
        let mobName = !mob.getName() || mob.getName() == 'MISSINGNO' ? `#o${mobId}#` : mob.getName();
        let stats = mob.getStats();
        let statsSize = Math.max(...[mob.getMaxHp(), stats.getPADamage(), stats.getMADamage()].map(v => v.toString().length));
        mobName = `[ #e#b${mobName}#k#n ] `;
        msgtext = `${getMobImage(mob)}\r\n${mobName}\r\n`;
        msgtext += `血量：${mob.getMaxHp().toString().padEnd(statsSize,' ')}\t\t蓝量：${mob.getMaxMp()}\r\n`;
        msgtext += `物攻：${stats.getPADamage().toString().padEnd(statsSize,' ')}\t\t物防：${stats.getPDDamage()}\r\n`;
        msgtext += `魔攻：${stats.getMADamage().toString().padEnd(statsSize,' ')}\t\t魔防：${stats.getMDDamage()}\r\n`;
        // msgtext += `　命中率：${stats.getMADamage()}\t\t　闪避率：${stats.getMDDamage()}\r\n`;
        if(CountItems <= 0) {
            msgtext += `\r\n\r\n没有掉落。`;
        } else {
            msgtext += `\r\n\r\n${'-'.repeat(28)}物品掉落列表一览${'-'.repeat(28)}\r\n\r\n`;
            // 遍历 table 对象的键，并设置其值为键名的长度
            Object.keys(table).forEach(key => {table[key] = Math.max(table[key],key.length)});
            let dropitemlist = {};
            dropall.filter(drop => drop.itemId > 0).forEach((drop) => {
                let itemName = ItemInformationProvider.getInstance().getName(drop.itemId);
                if (itemName != null) {
                    let itemChance = (drop.chance / 10000).toFixed(4);
                    // 更新 table 中对应的键值以记录最大长度
                    table['物品名称'] = Math.max(table['物品名称'], itemName.length);
                    table['基础掉率'] = Math.max(table['基础掉率'], itemChance.length / 2);
                    table['你的掉率'] = Math.max(table['你的掉率'], itemChance.length / 2);
                    // 确保 itemName 在结果对象中是唯一的键
                    // 如果 itemName 可能重复，可以添加索引或其它唯一标识
                    dropitemlist[drop.itemId] = {name : itemName , chance : itemChance , questid : drop.questid};
                }
            });
            // 确保所有值都是偶数
            Object.keys(table).forEach(key => table[key] = Math.ceil(table[key] / 2) * 2);
            msgtext += '#b' + Object.entries(table).map(([key,val]) => `${key.padEnd(val,'\t')}`).join('\t') + '#k\r\n';
            msgtext += Object.entries(dropitemlist).map(([itemId, { name, chance ,questid}]) => {
                    let msg = `#L${itemId}##v${itemId}#\r\n#b#e${name.padEnd(table['物品名称'] + countAllSymbols(name), '\t')}#k#n\t`;
                    msg += `${(chance + '%').padEnd(table['基础掉率'], '\t')}\t#d${(chance * player.getDropRate() * player.getFamilyDrop() + '%').padEnd(table['你的掉率'], '\t')}#k\r\n`;
                    msg += questid > 0 ? '#r[任务道具]#k '+QuestInfo.getInstance(questid).getName()+'\r\n' : '';
                    msg += '#l';
                    return msg;
                }
            ).join('\r\n');
        }
    }
    cm.sendLastLevel('main',msgtext,2); //这里会出现上一项+确定的对话框，如果点击确定则会进入到levelnull的方法里，估计是源码里没做判断。
}

/**
 * 提取字符串里的符号数量
 * @param str
 * @returns {*|number}
 */
function countAllSymbols(str) {
    return Math.ceil(str.match(/[^\u4e00-\u9fff]/g)?.length / 2) || 0;
}

/**
 * 以下函数在某些特定的情况下可能会导致客户端闪退
 * @param mob
 * @returns {string}
 */
function getMobImage(mob){
    let type = [null,'stand','fly']
        type = type[mob.getStats().getMovetype() + 1];    //-1=未知类型，0=陆地类型，1=飞天类型
    if(type == null) {
        return `#fUI/UIWindow.img/Maker/randomRecipe#`;     //没有怪物图片时显示一个问号。
    } else if (mob.getStats().getImgwidth() > 160 && mob.getStats().getImgheight() > 250) { //如果图片超过指定范围会造成客户端假死，因此这里需要替换成别的图片或者干脆不要。
        return `#fMap/Obj/Tdungeon.img/mushCatle/npc/0/0#\r\n(形象过大，不能展示)`;
    } else {
        //当前怪物ID最多7位数，不足7位数则需要在前面补0
        return `#fMob/${mob.getId().toString().padStart(7, '0')}.img/${type}/0#`;
    }
}

function getLevelImage(level,type) {
    let UI = []
        UI.push('Basic/LevelNo/');
        UI.push('Basic/ItemNo/');
        UI.push('UIWindow/SkillEx/SpNum/');
        UI.push('UIWindow/VegaSpell/Count/');
        UI.push('UIWindow/ToolTip/Equip/GrowthEnabled/');
        type = !type ? 0 : type;
        type = type > UI.length ? UI.length : type;
        UI = UI[type];
    return [...level.toString()].map(str => `#fUI/${UI + str}#`);
}
