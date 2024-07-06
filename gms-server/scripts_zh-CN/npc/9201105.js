/**
 *9201105 - Sage
 *@author Ronan
 */

function start() {
    if (cm.getMapId() == 610020005) {
        cm.sendOk("红木城堡就在前方，你今天取得了伟大的成就，向你致敬。穿过这片树林，进入城堡的大门。");
    } else {
        cm.sendOk("到目前为止，你的进展非常出色，干得好。然而，要想进入要塞，你必须面对并完成这一考验，继续前行。");
    }
    cm.dispose();
}