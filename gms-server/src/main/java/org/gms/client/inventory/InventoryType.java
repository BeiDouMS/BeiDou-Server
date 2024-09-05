/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gms.client.inventory;

import lombok.Getter;
import org.gms.util.I18nUtil;

/**
 * @author Matze
 */
@Getter
public enum InventoryType {
    UNDEFINED(0, I18nUtil.getMessage("InventoryType.UNDEFINED")),
    EQUIP(1, I18nUtil.getMessage("InventoryType.EQUIP")),
    USE(2, I18nUtil.getMessage("InventoryType.USE")),
    SETUP(3, I18nUtil.getMessage("InventoryType.SETUP")),
    ETC(4, I18nUtil.getMessage("InventoryType.ETC")),
    CASH(5, I18nUtil.getMessage("InventoryType.CASH")),
    CANHOLD(6, I18nUtil.getMessage("InventoryType.CANHOLD")),   //Proof-guard for inserting after removal checks
    EQUIPPED(-1, I18nUtil.getMessage("InventoryType.EQUIPPED")); //Seems nexon screwed something when removing an item T_T

    private final byte type;
    private final String name;

    InventoryType(int type, String name) {
        this.type = (byte) type;
        this.name = name;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    public static InventoryType getByType(byte type) {
        for (InventoryType l : InventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return UNDEFINED;
    }

    public static InventoryType getByWZName(String name) {
        return switch (name) {
            case "Install" -> SETUP;
            case "Consume" -> USE;
            case "Etc" -> ETC;
            case "Cash" -> CASH;
            case "Pet" -> CASH;
            default -> UNDEFINED;
        };
    }

    public boolean canChangeSlotMax() {
        // 如果需要支持更改现金的最大堆叠，可以修改这里
        return this == USE || this == ETC;
    }

    public boolean isEquip() {
        return this == EQUIP || this == EQUIPPED;
    }
}
