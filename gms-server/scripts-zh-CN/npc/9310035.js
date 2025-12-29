/*
	NPC Name: 		了凡
	Map(s): 		东方神州：大雄宝殿 (702100000)
	Description: 	和尚NPC，戏弄玩家并将其变成光头
	Creator : 		[ArthurZhu1992]
*/
let status = 0;
let playerHair = 0;

function start() {
    console.log("status: " + status)
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (status >= 0 && mode === -1) {
        cm.dispose();
        return;
    }

    if (status === -1) {
        status++
    } else if (mode === 1 && selection === -1 && type === 0) {
        status++;
    } else if (mode === 1 && selection === -1 && type === 1) {
        status++;
    } else if (mode === 0 && selection === -1 && type === 1) {
        status++;
    } else {
        status--;
    }


    if (status === 0) {
        // 保存玩家当前发型
        playerHair = cm.getPlayer().getHair();
        cm.sendNext("阿弥陀佛！施主，贫僧看你面带愁容，可是遇到什么烦心事了？");
    } else if (status == 1) {
        cm.sendNextPrev("施主啊，人生在世，烦恼总是有的，但不可执着。看施主如此烦恼，不如剃度出家，斩断三千烦恼丝？");
    } else if (status == 2) {
        cm.sendNextPrev("施主莫要急着回答，听贫僧讲个故事：从前有位施主，也如你一般烦恼重重，后来他出家为僧，法号'烦恼断'，从此烦恼皆消。");
    } else if (status == 3) {
        cm.sendNextPrev("哈哈，施主莫要误会，贫僧只是说说而已。不过，既然施主来此，不如体验一下出家人的生活？先从剃度开始如何？");
    } else if (status == 4) {
        cm.sendNextPrev("施主想“一刀剪断三千丝，往昔愁绪随风逝”么？贫僧看施主与佛有缘，这光头可是修行的第一步啊！");
    } else if (status == 5) {
        cm.sendYesNo("施主既然有此决心，贫僧就助你一臂之力。不过要提醒施主，一旦剃度，可就没有回头路了...确定要剃度吗？");
    } else if (status == 6) {
        if (mode === 1 && selection === -1 && type === 1) { // 玩家选择是
            // 将玩家变成光头
            let baldHair = 30030; // 光头发型ID
            cm.setHair(baldHair);
            cm.sendNext("阿弥陀佛！施主已剃度成功！从此烦恼丝尽断，心如明镜台。");
        } else {
            cm.sendNext("施主终究还是放不下红尘啊，也罢，出家修行需缘分，强求不得。");
            cm.dispose();
        }
    } else if (status == 7) {
        cm.sendNextPrev("施主现在已是光头，感觉如何？是不是觉得神清气爽，烦恼尽消？");
    } else if (status == 8) {
        cm.sendNextPrev("哈哈，开个玩笑，施主不必当真。这光头只是暂时的体验，不过贫僧看你还挺适合的呢！");
    } else if (status == 9) {
        cm.sendNextPrev("若施主真想恢复原样，可以去找美容师，或者等一段时间，头发自然会长出来的。");
    } else if (status == 10) {
        cm.sendNextPrev("好了，贫僧还有功课要做，就不多陪施主了。记住，烦恼都是自己找的，放下即是解脱。");
    } else if (status == 11) {
        cm.sendNextPrev("阿弥陀佛，善哉善哉！施主慢走，有缘再会！");
        cm.dispose();
    } else {
        cm.dispose();
    }
}