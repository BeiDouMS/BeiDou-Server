/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gms.client.autoban;

import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kevintjuh93
 */
public class AutobanManager {
    private static final Logger log = LoggerFactory.getLogger(AutobanManager.class);

    private final Character chr;
    private final Map<AutobanFactory, Integer> points = new HashMap<>();
    private final Map<AutobanFactory, Long> lastTime = new HashMap<>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;
    private final long[] spam = new long[20];
    private final int[] timestamp = new int[20];
    private final byte[] timestampcounter = new byte[20];


    public AutobanManager(Character chr) {
        this.chr = chr;
    }

    public void addPoint(AutobanFactory fac, String reason) {
        // 检查该类型是否被禁用：不计分、不打日志、不落库
        if (fac.isDisabled()) {
            return;
        }

        // GM/已封禁者豁免计分与处置，但仍打日志并落库（GM 用自己号验证检测点时能看到记录），此时 points 留空
        boolean exempt = chr.isGM() || chr.isBanned();
        // 获取生效的积分阈值
        int effectivePoints = fac.getEffectivePoints();
        Integer currentPoints = null;
        boolean reached = false;
        if (!exempt) {
            // 计分本身不受 use_auto_ban 控制：留痕模式下也能看到每个人的累计分；只有封禁处置受开关控制
            // 获取生效的过期时间
            long effectiveExpire = fac.getEffectiveExpiretime();

            if (lastTime.containsKey(fac)) {
                if (lastTime.get(fac) < (Server.getInstance().getCurrentTime() - effectiveExpire)) {
                    points.put(fac, points.get(fac) / 2); //So the points are not completely gone.
                }
            }
            if (effectiveExpire != -1) {
                lastTime.put(fac, Server.getInstance().getCurrentTime());
            }

            if (points.containsKey(fac)) {
                points.put(fac, points.get(fac) + 1);
            } else {
                points.put(fac, 1);
            }

            currentPoints = points.get(fac);
            reached = currentPoints >= effectivePoints;
        }

        if (GameConfig.getServerBoolean("use_auto_ban_log")) {
            // Lets log every single point too.
            log.info("Autoban - chr {} caused {} {}", Character.makeMapleReadable(chr.getName()), fac.name(), reason);
        }
        // 先留痕再处置，避免处置流程变更 Client 状态后再取账号信息
        AutobanLogger.record(chr, fac.name(), reached ? AutobanLogger.ACTION_AUTOBAN : AutobanLogger.ACTION_POINT, currentPoints, effectivePoints, reason);
        if (reached && GameConfig.getServerBoolean("use_auto_ban")) {
            chr.autoBan(reason);
        }
    }

    public void addMiss() {
        this.misses++;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses > 6) {
            samemisscount++;
        }
        if (samemisscount > 4) {
            chr.sendPolice("You will be disconnected for miss godmode.");
        }
        //chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
        else if (samemisscount > 0) {
            this.lastmisses = misses;
        }
        this.misses = 0;
    }

    //Don't use the same type for more than 1 thing
    public void spam(int type) {
        this.spam[type] = Server.getInstance().getCurrentTime();
    }

    public void spam(int type, int timestamp) {
        this.spam[type] = timestamp;
    }

    public long getLastSpam(int type) {
        return spam[type];
    }

    /**
     * Timestamp checker
     *
     * <code>type</code>:<br>
     * 1: Pet Food<br>
     * 2: InventoryMerge<br>
     * 3: InventorySort<br>
     * 4: SpecialMove<br>
     * 5: UseCatchItem<br>
     * 6: Item Drop<br>
     * 7: Chat<br>
     * 8: HealOverTimeHP<br>
     * 9: HealOverTimeMP<br>
     *
     * @param type type
     * @return Timestamp checker
     */
    public void setTimestamp(int type, int time, int times) {
        if (this.timestamp[type] == time) {
            this.timestampcounter[type]++;
            if (this.timestampcounter[type] >= times) {
                log.info("Autoban - Chr {} was caught spamming TYPE {} and has been disconnected", chr, type);
                // 先留痕再断线：disconnect 异步清空 Client 的账号信息，后落库会丢 account_name
                AutobanLogger.record(chr, AutobanLogger.TYPE_TIMESTAMP_SPAM, AutobanLogger.ACTION_DISCONNECT, null, times, "type=" + type);
                if (GameConfig.getServerBoolean("use_auto_ban")) {
                    chr.getClient().disconnect(false, false);
                }
            }
        } else {
            this.timestamp[type] = time;
            this.timestampcounter[type] = 0;
        }
    }
}
