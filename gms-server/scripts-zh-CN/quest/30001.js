/*
StudyJS-By HaiLong

BaseFunction:                                                             type |          mode               |  section
0:  sendYesNo(str) -[弹出Yes/No对话框]                                     1   |  是=1,否=0,结束=-1          |     \
1:  sendNext(str)  -[弹出带有下一个的对话框]                               0   |  下一项=1,结束=-1           |     \
2:  sendPrev(str)  -[弹出带有上一个的对话框]                               0   |  上一项=0,结束=-1           |     \
3:  sendOk(str)    -[弹出带有确定的对话框]                                 0   |  确定=1,结束=-1             |     \
4:  sendNextPrev(str)      -[弹出上&下一个的对话框]                        0   |  上一项=0,下一项=1,停止=-1  |     \
5:  sendAcceptDecline(str) -[弹出接受&拒绝的对话框]                        12  |  接受=1,拒绝=0,结束=-1      |     \  
6:  sendSimple(str) -[弹出带有选项(#L索引# xxx #l)的对话框]                4   |  选择=1,结束=0              |  选择的索引       
7:  sendStyle(str,int styles[]) -[弹出选择造型的对话框]                    0   |  确定=1,取消=0,结束=-1      |  选择的索引
8:  sendGetNumber(str,int def, int min, int max) -[弹出输入数字的对话框]   0   |  确定=1,结束=0              |  输入的数字
9:  setGetText(str) -[保存指定的字符串]                                    \   |          \                  |     \
10: sendGetText(str) -[弹出带有输入字符串的对话框]                         0   |  确定=1,结束=0              |     \
11: getText() -[返回sendGetText(str)/setGetText(str)寫入的字符串]       \   |          \                  |     \

 */

var status = -1;


function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            qm.sendNext("嗨~冒险者，时隔多年您又回到了记忆中的冒险岛呢，还记得吗？这是您刚踏入旅程时的第一站。");
        } 
		else if (status == 1) 
		{
			
			qm.sendAcceptDecline("也许关于职业和村子您早已烂熟于心，但我还是要向您介绍一下4个职业，因为这是我的职责，希望您能接受。");
        }
		else if (status == 2 && mode == 1)
		{
			qm.sendOk("非常感谢您，您按下#bALT#k键跳上履带，可以回忆大巨变以前的角色，在您参观完毕后，跟我的好友罗杰对话吧。")
            qm.forceStartQuest();
            qm.dispose();			
		}
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if(mode == 0 && type > 0) {
            qm.dispose();
            return;
        }
        
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
            qm.sendNext("您好，亲爱的冒险家，是莎莉让您来的吧？说明您已经熟悉了大巨变前的4大基本职业了，目前版本对标#bCMS079#k，因此也有了海盗,啊，你问#r战神和龙神#k？那是歪门邪道...");
        } else if (status == 1) {
			qm.sendOk("亲爱的冒险家，望您回到童年，享受以前无忧无虑的冒险欢乐时光吧！");
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}