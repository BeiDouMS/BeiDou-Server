/* @Author Ronan
        Name: Heracle
        Map(s): Guild Headquarters
        Info: Hall of Fame
        Script: credits.js
*/

var status;

var name_tree = [];
var role_tree = [];
var name_cursor, role_cursor;

// 请将新的服务器名称将被添加到在servers数组里，按照构建的时间顺序。
var servers = ["BeiDou", "Cosmic", "HeavenMS", "MapleSolaxia", "MoopleDEV", "BubblesDEV", "MetroMS", "OdinMS", "Contributors"];
var servers_history = [];

function addPerson(name, role) {
    name_cursor.push(name);
    role_cursor.push(role);
}

function setHistory(from, to) {
    servers_history.push([from, to]);
}

/*
function writeServerStaff_MapleNext() {
        addPerson("John Doe", "The role");

        setHistory(INITIAL_YEAR [, CURRENT_YEAR]);
}
*/

function writeServerStaff_BeiDou() {
    addPerson("昨日小睡", "开发者");
    addPerson("leevccc", "开发者");
    addPerson("香辣汉堡", "开发者");
    addPerson("TokyoEric", "贡献者");
    addPerson("刘波波", "贡献者");
    addPerson("huash", "贡献者");
    addPerson("Tomddlee", "贡献者");
    addPerson("Datas", "贡献者");
    addPerson("stanx5", "贡献者");
    addPerson("jarecl", "贡献者");
    addPerson("g9502995", "贡献者");
    addPerson("kengwon", "贡献者");
    addPerson("小蜗", "贡献者");
    addPerson("r09er", "贡献者");
    addPerson("li-coder-313", "贡献者");

    setHistory(2024, 2024);
}

function writeServerStaff_Cosmic() {
    addPerson("Ponk", "开发者");

    setHistory(2021, 2024);
}

function writeServerStaff_HeavenMS() {
    addPerson("Ronan", "开发者");
    addPerson("Vcoc", "自由开发者");
    addPerson("Thora", "贡献者");
    addPerson("GabrielSin", "贡献者");
    addPerson("Masterrulax", "贡献者");
    addPerson("MedicOP", "兼职开发人员");

    setHistory(2015, 2019);
}

function writeServerStaff_MapleSolaxia() {
    addPerson("Aria", "管理员");
    addPerson("Twdtwd", "管理员");
    addPerson("Exorcist", "开发者");
    addPerson("SharpAceX", "开发者");
    addPerson("Zygon", "自由开发者");
    addPerson("SourMjolk", "游戏GM");
    addPerson("Kanade", "游戏GM");
    addPerson("Kitsune", "游戏GM");

    setHistory(2014, 2015);
}

function writeServerStaff_MoopleDEV() {
    addPerson("kevintjuh93", "开发者");
    addPerson("hindie93", "贡献者");
    addPerson("JuniarZ-", "贡献者");

    setHistory(2010, 2012);
}

function writeServerStaff_BubblesDEV() {
    addPerson("David!", "开发者");
    addPerson("Moogra", "开发者");
    addPerson("XxOsirisxX", "贡献者");
    addPerson("MrMysterious", "贡献者");

    setHistory(2009, 2010);
}

function writeServerStaff_MetroMS() {
    addPerson("David!", "开发者");
    addPerson("XxOsirisxX", "贡献者");
    addPerson("Generic", "贡献者");

    setHistory(2009, 2009);
}

function writeServerStaff_OdinMS() {
    addPerson("Serpendiem", "管理员");
    addPerson("Frz", "开发者");
    addPerson("Patrick", "开发者");
    addPerson("Matze", "开发者");
    addPerson("Vimes", "开发者");

    setHistory(2007, 2008);
}

function writeServerStaff_Contributors() {
    addPerson("IxianMace", "贡献者");
    addPerson("Conrad", "贡献者");
    addPerson("inhyuk", "贡献者");
    addPerson("Jayd", "贡献者");
    addPerson("Dragohe4rt", "贡献者");
    addPerson("Jvlaple", "贡献者");
    addPerson("Stereo", "贡献者");
    addPerson("AngelSL", "贡献者");
    addPerson("Lerk", "贡献者");
    addPerson("Leifde", "贡献者");
    addPerson("ThreeStep", "贡献者");
    addPerson("RMZero213", "贡献者");
    addPerson("ExtremeDevilz", "贡献者");
    addPerson("aaroncsn", "贡献者");
    addPerson("xQuasar", "贡献者");
    addPerson("Xterminator", "贡献者");
    addPerson("XoticStory", "贡献者");
}

function writeAllServerStaffs() {
    for (var i = 0; i < servers.length; i++) {
        name_cursor = [];
        role_cursor = [];

        var srvName = servers[i];
        this["writeServerStaff_" + srvName]();

        name_tree.push(name_cursor);
        role_tree.push(role_cursor);
    }
}

function start() {
    status = -1;
    writeAllServerStaffs();
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            var sendStr = "以下是关于所有参与了本服务器建设的开发者的历史:\r\n\r\n"
            for (var i = 0; i < servers.length; i++) {
                var hist = servers_history[i];

                if (hist && hist.length > 0) {
                    sendStr += "#L" + i + "##b" + servers[i] + "#k  --  " + ((hist[0] != hist[1]) ? hist[0] + " ~ " + hist[1] : hist[0]) + "#l\r\n";
                } else {
                    sendStr += "#L" + i + "##b" + servers[i] + "#k#l\r\n";
                }
            }

            cm.sendSimple(sendStr);
        } else if (status == 1) {
            var lvName, lvRole;

            for (var i = 0; i < servers.length; i++) {
                if (selection == i) {
                    lvName = name_tree[i];
                    lvRole = role_tree[i];
                    break;
                }
            }

            var sendStr = "以下是 #b" + servers[selection] + "#k 的主要人员:\r\n\r\n";
            for (var i = 0; i < lvName.length; i++) {
                sendStr += "  #L" + i + "# " + lvName[i] + " - " + lvRole[i];
                sendStr += "#l\r\n";
            }

            cm.sendPrev(sendStr);
        } else {
            cm.dispose();
        }
    }
}
