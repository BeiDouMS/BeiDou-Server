/**
 *9201100 - Taggrin
 *@author Ronan
 */

function start() {
    if (cm.getQuestStatus(8224) == 2) {
        cm.sendOk("你好，同伴。如果你需要帮助，可以尝试与我们的成员交谈。");
    } else {
        cm.sendOk("你好，陌生人。我们是著名的渡鸦爪佣兵团，我是他们的领袖。");
    }

    cm.dispose();
}