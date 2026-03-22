/**
 * 双倍活动事件
 */

var start2xTime = "20:00"; //开启活动时间
var end2xTime = "21:00"; //结束活动时间

// 事件初始化
function init() {

    // 仅在频道1初始化（下列方法均为全服处理，非仅频道1，此判断是为了防止重复提示）
    if (em.getChannelServer().getId() == 1) {

        // 将频道加成设置为初始值
        em.getWorldServer().setExpRate(1);
        em.getWorldServer().setServerMessage("");

        scheduleNew();

    }
}

function scheduleNew() {

    const isOpen2xExp = isOpenTime(start2xTime, end2xTime);
    const expRate = em.getWorldServer().getExpRate();
    

    // 开启双倍
    if (isOpen2xExp && expRate !== 2) {
        var msgText = `狩猎双倍经验活动已开启！时间：${start2xTime} ~ ${end2xTime}`;
        em.getWorldServer().setExpRate(2);
        em.getWorldServer().dropMessage(5, msgText);
        em.getWorldServer().setServerMessage(msgText);
    }

    // 关闭双倍
    if (!isOpen2xExp && expRate !== 1) {
        em.getWorldServer().setExpRate(1);
        em.getWorldServer().dropMessage(5, "狩猎双倍经验活动已结束！");
        em.getWorldServer().setServerMessage("");
    }


    em.schedule("scheduleNew", 1000);

}



/**
 * 判断是否已经达到活动时间内
 * @Desc   无
 * @Author Ming
 * @Date   2026-03-22
 * @param  {[type]}   start 开始时间：20:00
 * @param  {[type]}   end   结束时间：20:30
 * @return {Boolean}
 */
function isOpenTime(start, end) {
    const currentTime = formatTime('HH:mm');
    const timeToMinutes = (timeStr) => {
        const [hours, minutes] = timeStr.split(':').map(Number);
        return hours * 60 + minutes;
    };
    const currentTotal = timeToMinutes(currentTime);
    const startTotal = timeToMinutes(start);
    const endTotal = timeToMinutes(end);
    return currentTotal >= startTotal && currentTotal <= endTotal;
}

/**
 * 格式化当前时间
 * @Desc   无
 * @Author Ming
 * @Date   2026-03-22
 * @param  {String}   format
 * @return {String} 
 */
function formatTime(format = 'YYYY-MM-DD HH:mm:ss') {
    const date = = new Date();
    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const day = String(targetDate.getDate()).padStart(2, '0'); 
    const hours = String(targetDate.getHours()).padStart(2, '0');
    const minutes = String(targetDate.getMinutes()).padStart(2, '0');
    const seconds = String(targetDate.getSeconds()).padStart(2, '0');
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}
