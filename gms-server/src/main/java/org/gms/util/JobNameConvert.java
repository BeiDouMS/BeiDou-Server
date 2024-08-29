package org.gms.util;

public class JobNameConvert {
    // "0": "新手",
    //	"100": "战士",
    //	"110": "剑客",
    //	"111": "勇士",
    //	"112": "英雄",
    //	"120": "准骑士",
    //	"121": "骑士",
    //	"122": "圣骑士",
    //	"130": "枪战士",
    //	"131": "龙骑士",
    //	"132": "黑骑士",
    //	"200": "魔法师",
    //	"210": "法师（火毒）",
    //	"211": "巫师（火毒）",
    //	"212": "魔导师（火毒）",
    //	"220": "法师（冰雷）",
    //	"221": "巫师（冰雷）",
    //	"222": "魔导师（冰雷）",
    //	"230": "牧师",
    //	"231": "祭司",
    //	"232": "主教",
    //	"300": "弓箭手",
    //	"310": "猎人",
    //	"311": "射手",
    //	"312": "神射手",
    //	"320": "弩弓手",
    //	"321": "游侠",
    //	"322": "箭神",
    //	"400": "飞侠",
    //	"410": "刺客",
    //	"411": "无影人",
    //	"412": "隐士",
    //	"420": "侠客",
    //	"421": "独行客",
    //	"422": "侠盗",
    //	"500": "海盗",
    //	"510": "拳手",
    //	"511": "斗士",
    //	"512": "冲锋队长",
    //	"520": "火枪手",
    //	"521": "大副",
    //	"522": "船长",
    //	"1000": "初心者",
    //	"1100": "魂骑士",
    //	"1110": "魂骑士",
    //	"1111": "魂骑士",
    //	"1112": "魂骑士",
    //	"1200": "炎术士",
    //	"1210": "炎术士",
    //	"1211": "炎术士",
    //	"1212": "炎术士",
    //	"1300": "风灵使者",
    //	"1310": "风灵使者",
    //	"1311": "风灵使者",
    //	"1312": "风灵使者",
    //	"1400": "夜行者",
    //	"1410": "夜行者",
    //	"1411": "夜行者",
    //	"1412": "夜行者",
    //	"1500": "奇袭者",
    //	"1510": "奇袭者",
    //	"1511": "奇袭者",
    //	"1512": "奇袭者",
    //	"2000": "战童",
    //	"2100": "战神",
    //	"2110": "战神",
    //	"2111": "战神",
    //	"2112": "战神"
    //请写一个转换类,把对应数字转换成对应的职业名称
    public static String convertJobName(int job) {
        switch (job) {
            case 0:
                return "新手";
            case 100:
                return "战士";
            case 110:
                return "剑客";
            case 111:
                return "勇士";
            case 112:
                return "英雄";
            case 120:
                return "准骑士";
            case 121:
                return "骑士";
            case 122:
                return "圣骑士";
            case 130:
                return "枪战士";
            case 131:
                return "龙骑士";
            case 132:
                return "黑骑士";
            case 200:
                return "魔法师";
            case 210:
                return "法师（火毒）";
            case 211:
                return "巫师（火毒）";
            case 212:
                return "魔导师（火毒）";
            case 220:
                return "法师（冰雷）";
            case 221:
                return "巫师（冰雷）";
            case 222:
                return "魔导师（冰雷）";
            case 230:
                return "牧师";
            case 231:
                return "祭司";
            case 232:
                return "主教";
            case 300:
                return "弓箭手";
            case 310:
                return "猎人";
            case 311:
                return "射手";
            case 312:
                return "神射手";
            case 320:
                return "弩弓手";
            case 321:
                return "游侠";
            case 322:
                return "箭神";
            case 400:
                return "飞侠";
            case 410:
                return "刺客";
            case 411:
                return "无影人";
            case 412:
                return "隐士";
            case 420:
                return "侠客";
            case 421:
                return "独行客";
            case 422:
                return "侠盗";
            case 500:
                return "海盗";
            case 510:
                return "拳手";
            case 511:
                return "斗士";
            case 512:
                return "冲锋队长";
            case 520:
                return "火枪手";
            case 521:
                return "大副";
            case 522:
                return "船长";
            case 1000:
                return "初心者(骑士团)";
            case 1100:
                return "魂骑士(一转)";
            case 1110:
                return "魂骑士(二转)";
            case 1111:
                return "魂骑士(三转)";
            case 1112:
                return "魂骑士(四转)";
            case 1200:
                return "炎术士(一转)";
            case 1210:
                return "炎术士(二转)";
            case 1211:
                return "炎术士(三转)";
            case 1212:
                return "炎术士(四转)";
            case 1300:
                return "风灵使者(一转)";
            case 1310:
                return "风灵使者(二转)";
            case 1311:
                return "风灵使者(三转)";
            case 1312:
                return "风灵使者(四转)";
            case 1400:
                return "夜行者(一转)";
            case 1410:
                return "夜行者(二转)";
            case 1411:
                return "夜行者(三转)";
            case 1412:
                return "夜行者(四转)";
            case 1500:
                return "奇袭者(一转)";
            case 1510:
                return "奇袭者(二转)";
            case 1511:
                return "奇袭者(三转)";
            case 1512:
                return "奇袭者(四转)";
            case 2000:
                return "战童";
            case 2100:
                return "战神(二转)";
            case 2110:
                return "战神(三转)";
            case 2111:
                return "战神(三转)";
            case 2112:
                return "战神(四转)";
            default:
                return "未知职业";


        }
    }
}
