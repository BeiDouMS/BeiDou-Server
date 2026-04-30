package org.gms.client.fake;

import org.gms.client.Job;
import org.gms.net.packet.OutPacket;
import org.gms.net.opcodes.SendOpcode;
import java.awt.Point;
import org.gms.net.packet.Packet;

/**
 * 假人数据包构造器
 * 负责将假人数据编码为Netty可发送的数据包
 * 
 * 核心逻辑参考 PacketCreator.spawnPlayerMapObject()
 */
public class FakePlayerPacketCreator {
    
    /**
     * 创建假人生成包（其他玩家可见）
     * 
     * @param fake 假人对象
     * @return 可发送的数据包
     */
    public static Packet spawnFakePlayer(FakePlayer fake) {
        OutPacket p = OutPacket.create(SendOpcode.SPAWN_PLAYER);
        
        // === 基础信息 ===
        p.writeInt(fake.getId());              // 角色ID（负数）
        p.writeByte(fake.getLevel());           // 等级
        p.writeString(fake.getName());          // 名称
        
        // === 公会信息 ===
        if (fake.getGuildId() < 1) {
            p.writeString("");                  // 无公会
            p.writeBytes(new byte[6]);          // 填充
        } else {
            p.writeString(fake.getGuildName()); // 公会名
            p.writeShort(0);                    // Logo背景
            p.writeByte(0);                     // Logo背景颜色
            p.writeShort(0);                    // Logo
            p.writeByte(0);                     // Logo颜色
        }
        
        // === Buff数据（假人无Buff）===
        p.writeShort(0);                        // Buff数量
        
        // === 职业 ===
        p.writeShort(fake.getJob().getId());
        
        // === 外观 ===
        addFakeCharLook(p, fake);
        
        // === 其他显示数据 ===
        p.writeInt(0);                          // 巧克力心数量
        p.writeInt(0);                          // 物品效果
        p.writeInt(0);                          // 椅子
        
        // === 位置 ===
        Point pos = fake.getPosition();
        if (pos != null) {
            p.writePos(new Point(pos.x, pos.y - 42));  // 进入地图时Y轴偏移
        } else {
            p.writePos(new Point(0, 0));
        }
        p.writeByte(6);                         // 动作（站立）
        
        // === 其他数据 ===
        p.writeShort(0);                        // Foothold
        p.writeByte(0);                         // 未知
        
        // === 宠物（假人无宠物）===
        for (int i = 0; i < 3; i++) {
            p.writeByte(0);                     // 无宠物
        }
        
        // === 坐骑（假人无坐骑）===
        p.writeInt(0);                          // 无坐骑
        
        // === 协调者数据 ===
        p.writeInt(0);
        p.writeInt(0);
        p.writeInt(0);
        p.writeInt(0);
        
        // === 社交数据 ===
        p.writeInt(0);                          // 结婚戒指
        p.writeInt(0);                          // 伴侣ID
        p.writeInt(0);                          // 家族数据
        
        return p;
    }
    
    /**
     * 编码假人外观数据
     */
    private static void addFakeCharLook(OutPacket p, FakePlayer fake) {
        p.writeByte(fake.getGender());          // 性别
        p.writeByte(fake.getSkinColor().getId()); // 肤色
        p.writeInt(fake.getFace());             // 脸型
        p.writeBool(true);                      // 未知（mega=false）
        p.writeInt(fake.getHair());             // 发型
        
        // === 装备 ===
        int[] equips = fake.getEquipData();
        for (int i = 0; i < equips.length; i += 2) {
            if (i + 1 < equips.length) {
                p.writeByte(equips[i]);         // 位置
                p.writeInt(equips[i + 1]);      // 物品ID
            }
        }
        p.writeByte(0xFF);                      // 装备结束标记
        
        // === 面具装备 ===
        p.writeByte(0xFF);                      // 无面具
        
        // === 现金武器 ===
        p.writeInt(fake.getCashWeapon());
    }
    
    /**
     * 创建假人移除包
     */
    public static Packet removeFakePlayer(int fakeId) {
        OutPacket p = OutPacket.create(SendOpcode.REMOVE_PLAYER_FROM_MAP);
        p.writeInt(fakeId);
        p.writeByte(0);                         // 离开原因
        return p;
    }
    
    /**
     * 创建假人信息包（点击角色时显示）
     */
    public static Packet fakeCharInfo(FakePlayer fake) {
        OutPacket p = OutPacket.create(SendOpcode.CHAR_INFO);
        
        p.writeInt(fake.getId());               // 角色ID
        p.writeByte(fake.getLevel());           // 等级
        p.writeShort(fake.getJob().getId());    // 职业
        p.writeShort(fake.getFame());           // 人气
        
        // === 组队/Guild/联盟 ===
        p.writeBool(false);                     // 无组队
        p.writeString(fake.getGuildName());     // 公会名
        
        // === 好友信息 ===
        p.writeByte(0);                         // 好友数量
        
        // === 配偶信息 ===
        p.writeByte(0);                         // 无配偶
        
        return p;
    }
    
    /**
     * 创建假人聊天包（假人显示说话）
     */
    public static Packet fakeChat(int fakeId, String message, boolean white) {
        OutPacket p = OutPacket.create(SendOpcode.CHATTEXT);
        p.writeInt(fakeId);
        p.writeBool(white);
        p.writeString(message);
        p.writeBool(false);                     // 不显示在顶部
        return p;
    }
}
