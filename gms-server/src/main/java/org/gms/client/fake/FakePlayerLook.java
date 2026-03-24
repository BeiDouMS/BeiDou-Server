package org.gms.client.fake;

/**
 * 假人装扮数据
 * 包含角色外观的所有信息
 *
 * @param gender   性别: 0=男, 1=女
 * @param skinColor 肤色ID
 * @param face     脸型ID
 * @param hair     发型ID
 * @param equips   装备数组: [位置, 物品ID, 位置, 物品ID, ...]
 * @param weaponId 武器ID (cash weapon slot)
 */
public record FakePlayerLook(
        byte gender,
        int skinColor,
        int face,
        int hair,
        int[] equips,
        int weaponId
) {
    /**
     * 创建空装扮（默认外观）
     */
    public static FakePlayerLook defaultLook() {
        return new FakePlayerLook(
                (byte) 0,
                0,
                20000,
                30000,
                new int[]{},
                0
        );
    }
}
