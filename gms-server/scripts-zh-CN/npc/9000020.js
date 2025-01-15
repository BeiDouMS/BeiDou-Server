/*
    This file is part of the BeiDou MapleStory Server
    Copyright (C) 2025 Magical-H <@Magical-H>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
var travelAgency = '\u5317\u6597\u822A\u65C5';
var fromMapID = null;       //出发前的地图
var currentMapID = null;    //当前地图
var talkIndex = 0;
var dynamicsFee = true, FeeMin = 5000, FeeMax = 50000;     //是否启用动态费用，最小费用，最大费用。
var travelConfig = {
    Maps : [
        {
            Area: '示例地区',
            Name: '示例地图',
            MapID: 0,
            Fee: 3000,
            Npc: 9201135,
            Portal: 0,
            Desc : ['介绍描述']
        }
    ]
};

var travelMapObj = travelConfig.Maps[0];    //预处理，方便后续调用travelMapObj时可以快速使用对应键值。

travelMapObj = null;
travelConfig.Maps = [];  //清空示例内容

function start(){
    if(currentMapID === null) {
        currentMapID = cm.getMapId();
        fromMapID = cm.getPlayer().peekSavedLocation("WORLDTOUR");

        addTravelMap('东方神州','上海外滩',701000000,1,10000,[
            '上海外滩是上海市的标志性景点之一，以其壮丽的天际线而闻名。这里是黄浦江畔的一条长堤路，两旁矗立着众多历史建筑和现代化摩天大楼。',
            '你可以在这里欣赏到浦东陆家嘴的金融区、外白渡桥以及历史悠久的老建筑群。夜晚，这里的灯光璀璨夺目，非常适合散步或拍照留念。',
            '此外，外滩附近还有许多美食街和特色餐厅，你可以品尝到地道的上海小吃，如生煎包、小笼包等。'
        ]);

        addTravelMap('东方神州','嵩山镇',702000000,8,10000,[
            '嵩山镇是一个充满历史韵味的小城镇，位于中国的河南省登封市。这里有许多古老的寺庙和文化遗产，其中最著名的是少林寺，被誉为武术发源地之一。',
            '你可以参观少林寺的千年古刹，体验正宗的禅宗文化。在嵩山镇，你还可以欣赏到美丽的山水风光，感受大自然的魅力。',
            '此外，嵩山镇周边有许多传统村落和农家乐，你可以品尝到当地的农家菜，了解中原地区的风土人情。'
        ]);

        addTravelMap('新加坡','驳船码头城',541000000,4,10000,[
            '驳船码头城是新加坡的一个充满活力的历史区域，它将现代都市生活与殖民时期的建筑完美融合。这里曾经是繁忙的贸易港口，现在则是夜生活的中心，拥有各种酒吧、餐厅和娱乐场所。',
            '当您漫步在这个充满魅力的地方时，不妨停下来品尝一下当地的美食，如辣椒蟹和海南鸡饭。此外，还可以参加河畔游船之旅，欣赏美丽的天际线。。'
        ]);

        addTravelMap('马来西亚','吉隆大都市',550000000,4,10000,[
            '如果你想在乐观的环境中感受热带的炎热，马来西亚的居民非常欢迎你。此外，大都市本身就是当地经济的核心，众所周知，这个地方总是有事情可做或参观。',
            '一到那里，我强烈建议你安排一次去甘榜村的旅行。为什么？你肯定已经知道奇幻主题公园《幽灵世界》了吧？不它只是把最好的主题公园放在那里，值得一游！'
        ],9201135);
        addTravelMap('日本','蘑菇神社',800000000,5,10000,[
            '如果你想感受日本的精髓，没有什么比参观日本文化大熔炉更好的了。蘑菇神社是一个神话般的地方，供奉着古代无与伦比的蘑菇神。',
            '看看为蘑菇神服务的女巫，我强烈建议尝试日本街头出售的章鱼烧、烤肉和其他美味的食物。'
        ]);
    }
    if (travelConfig.Maps.some(map => map.MapID === currentMapID)) {//当前在某一个旅游地图
        cm.sendNextSelectLevel('Comback','旅行怎么样？你喜欢吗？\r\n#b\r\n#L0#是的，我已经旅行完了，要回去了。\r\n#L1#不，我想继续探索这个地方。');
    } else {
        // 计算每个字段的最大长度
        let maxAreaLength = Math.max(...travelConfig.Maps.map(map => map.Area.length));
        let maxNameLength = Math.max(...travelConfig.Maps.map(map => map.Name.length));
        let text = `如果你觉得日常生活的节奏有些单调，不妨换个心情，去外面的世界探索一番吧？\r\n没有什么能比沉浸在不同的文化中，每分钟都有新发现更让人愉悦了！是时候计划一次旅行了。\r\n我们#b#e${travelAgency}#k#n强烈建议你来一场#b世界之旅#k！\r\n担心旅行费用吗？别担心！\r\n我们#b#e${travelAgency}#k#n已经为你精心制定了以下旅游计划，让你轻松享受旅程：\r\n\r\n`;
        travelConfig.Maps.forEach((map, index) => {
            const line = `#L${index}##b【${map.Area.padStart(maxAreaLength,'　')}】\t\t${map.Name.padEnd(maxNameLength,'　')}#k\t\t#fUI/Basic.img/BtCoin/normal/0#${map.Fee.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")}#l\r\n`;            // 使用模板字符串构建每一行的格式，并根据最大长度填充空格
            text += line;
        });
        cm.sendNextSelectLevel('GoTravel',text);
    }

}

function level() {
    cm.dispose();
}
function levelnull() {
    level();
}
function leveldispose() {
    level();
}
/**
 * 从sendNextSelectLevel发送过来的交互函数
 * 返回旅游之前的地图
 * @param index
 */
function levelComback(index) {
    if (index !== undefined) {//判断是否为自身调用。
        if (index === 0) {//前面的index只有0和1，所以if为最优解。
            cm.sendNextLevel('Comback',`好的，我将送你回到#b#m${fromMapID}##k`); //这里重复调用自身函数时不含值，所以第一个if需要做判断。
        } else {
            cm.sendOkLevel('','如果你想回去了可以随时找我，祝你旅途愉快！');
        }
    } else {
        if (fromMapID !== null) {    //能查询到来之前的地图则送回去。
            cm.warp(fromMapID);
            level();
        } else {
            cm.sendNextLevel('','你没有来时的记录！你这个愚蠢的非法偷渡者！');
        }
    }
}

/**
 * 从sendNextSelectLevel发送过来的交互函数
 * 出门旅游
 * 这种函数写法比较复杂，不推荐新手使用
 * @param index
 */
function levelGoTravel(index) {
    if (index === undefined && talkIndex === -1) {//判断是否为自身调用。
        if (travelMapObj === null) {
            cm.sendOkLevel('','如果你想去旅游，请你通过合法渠道进行旅游。');
        } else {
            if (cm.getMeso() < travelMapObj.Fee) {
                cm.sendOkLevel('','哎呀，看起来你的金币不足，无法进行这次世界之旅。\\r\\n不过别担心，当你有足够的金币时，随时欢迎来找我！\\r\\n祝你早日攒够金币，开启一段精彩的旅程！');
            } else {
                cm.getPlayer().saveLocation("WORLDTOUR");//保存当前地图
                cm.gainMeso(-travelMapObj.Fee);//扣除金币
                cm.warp(travelMapObj.MapID, travelMapObj.Portal);//传送地图
                level();//结束对话
            }
        }
    } else {//非自身调用
        if (index < 0 || index > travelConfig.Maps.length - 1) {//预防索引越界
            cm.sendOkLevel('','如果你想去旅游，请你不要尝试通过非法渠道旅游。');
        } else {
            let text;
            if (!travelMapObj) travelMapObj = travelConfig.Maps[index];
            if (!travelMapObj) {//预防旅游对象为空 XD
                cm.sendOkLevel('','由于你没有对象，无法进行旅游。');
            } else {
                if (talkIndex >= 0 || talkIndex < travelMapObj.Desc.length - 1) {
                    text = travelMapObj.Desc[talkIndex];
                }
                talkIndex = talkIndex < travelMapObj.Desc.length - 1 ? talkIndex + 1 : -1
                cm.sendNextLevel('GoTravel', text);
            }
        }
    }
}

/**
 * 添加一个新的旅行地图到旅行配置中。
 *
 * @param {string} Area - 旅行地点的地区名称。
 * @param {string} Name - 旅行地图的名称。
 * @param {number} MapID - 地图的唯一标识符。
 * @param {number} Portal - 用于传送到该地图的传送门ID。
 * @param {number} Fee - 到达该地图所需的费用。
 * @param {string|string[]} Desc - 旅行地图的描述信息，可以是字符串或字符串数组。
 * @param {number} [Npc] - 可选参数，NPC ID，该NPC将作为向导在该地点提供服务。
 */
function addTravelMap(Area,Name,MapID,Portal,Fee,Desc,Npc) {
    let l_desc = !Array.isArray(Desc) ? [Desc] : Desc;
    let newMap = {
        Area: Area,
        Name: Name,
        MapID: MapID,
        Portal: Portal,
        Fee: dynamicsFee ? calculateTravelFee(currentMapID,MapID) : Fee,
        Desc: l_desc
    };
    if (Npc) {//替代向导
        newMap[Npc] = Npc;
        newMap.Desc.push(`我们目前为您提供这个旅行地点：#b${Name}#k。\r\n将由与我们合作的当地向导 #b#p${Npc}##k 在那里作为您的旅行向导为您服务。\r\n请放心，目的地数量将会随着时间的推移而增加。`);
    }
    newMap.Desc.push(`现在让我们立刻前往 #b#e${Name}#k#n 吧！出发！`);
    travelConfig.Maps.push(newMap);
}
/**
 * 根据当前地图ID和目标地图ID计算旅行费用。
 *
 * @param {number} MapID - 当前地图的ID。
 * @param {number} targetMapID - 目标地图的ID。
 * @returns {number} 计算出的旅行费用。
 */
function calculateTravelFee(MapID, targetMapID) {
    const baseFee = FeeMin || 10000; // 基础费用
    const maxFee = FeeMax || 100000; // 最高费用
    const distanceFactor = 0.00004; // 每个单位距离增加的费用
    const distance = Math.abs(targetMapID - MapID);// 计算地图ID的绝对差值
    let fee = baseFee + (distance * distanceFactor);// 计算费用
    return Math.min(fee, maxFee) | 0;// 确保费用不超过最高费用且为整数
}