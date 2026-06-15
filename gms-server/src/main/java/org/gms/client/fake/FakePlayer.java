package org.gms.client.fake;

import org.gms.client.Job;
import org.gms.client.SkinColor;
import java.awt.Point;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 假人对象
 * 用于在地图上显示虚假玩家，穿着随机装扮，仅做展示用途
 *
 * <p>假人通过 Netty 发包实现，不占用真实玩家连接。
 * 假人 ID 使用负数，避免与真实玩家冲突。</p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Data
public class FakePlayer {

    /**
     * ID 计数器，从 -1000000 开始递减
     */
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(-1_000_000);

    /**
     * 唯一标识（负数）
     */
    private final int id;

    /**
     * 显示名称
     */
    private String name;

    /**
     * 等级 (1-200)
     */
    private int level;

    /**
     * 职业
     */
    private Job job;

    /**
     * 人气值
     */
    private int fame;

    /**
     * 地图位置
     */
    private Point position;

    /**
     * 站姿
     */
    private int stance;

    /**
     * 外观装扮
     */
    private FakePlayerLook look;

    /**
     * 所在地图 ID
     */
    private int mapId;

    /**
     * 公会 ID (-1 = 无公会)
     */
    private int guildId;

    /**
     * 公会名称
     */
    private String guildName;

    /**
     * 创建假人实例
     *
     * @param name 显示名称
     */
    public FakePlayer(String name) {
        this.id = ID_COUNTER.decrementAndGet();
        this.name = name;
        this.level = 1;
        this.job = Job.BEGINNER;
        this.fame = 0;
        this.position = new Point(0, 0);
        this.stance = 0;
        this.look = FakePlayerLook.defaultLook();
        this.mapId = 0;
        this.guildId = -1;
        this.guildName = "";
    }

    /**
     * 获取性别
     *
     * @return 性别 (0=男, 1=女)
     */
    public byte getGender() {
        return look.gender();
    }

    /**
     * 获取皮肤颜色
     *
     * @return 皮肤颜色枚举
     */
    public SkinColor getSkinColor() {
        return SkinColor.getById(look.skinColor());
    }

    /**
     * 获取脸型 ID
     *
     * @return 脸型 ID
     */
    public int getFace() {
        return look.face();
    }

    /**
     * 获取发型 ID
     *
     * @return 发型 ID
     */
    public int getHair() {
        return look.hair();
    }

    /**
     * 获取装备数据（用于发包）
     *
     * @return 装备数组
     */
    public int[] getEquipData() {
        return look.equips();
    }

    /**
     * 获取现金武器 ID
     *
     * @return 现金武器 ID
     */
    public int getCashWeapon() {
        return look.weaponId();
    }

    /**
     * 检查是否在指定地图
     *
     * @param mapId 地图 ID
     * @return 如果在指定地图返回 true
     */
    public boolean isInMap(int mapId) {
        return this.mapId == mapId;
    }

    /**
     * 检查是否有公会
     *
     * @return 如果有公会返回 true
     */
    public boolean hasGuild() {
        return this.guildId >= 0;
    }

    @Override
    public String toString() {
        return String.format("FakePlayer{id=%d, name='%s', level=%d, job=%s, map=%d}",
                id, name, level, job, mapId);
    }
}
