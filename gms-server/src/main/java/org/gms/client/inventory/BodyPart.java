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

public enum BodyPart {
    EQUIPPED_BASE(0),
    HAIR(0),
    CAP(1),
    FACE_ACCESSORY(2),
    EYE_ACCESSORY(3),
    EAR_ACCESSORY(4),
    COAT(5),
    LONGCOAT(5), // Shared BodyPart for top / overall
    PANTS(6),
    SHOES(7),
    GLOVE(8),
    CAPE(9),
    SHIELD(10),
    WEAPON(11),

    RING_1(12),
    RING_2(13),
    PET_EQUIP_1(14),
    RING_3(15),
    RING_4(16),
    PENDANT(17),
    TAMING_MOB(18),
    SADDLE(19),
    MOB_EQUIP(20),

    PET_LABEL_RING_1(21),
    PET_ITEM_POUCH_1(22),
    PET_MESO_MAGNET_1(23),
    PET_AUTO_HP_POUCH(24),
    PET_AUTO_MP_POUCH(25),
    PET_WING_BOOTS_1(26),
    PET_BINOCULAR_1(27),
    PET_MAGIC_SCALES_1(28),
    PET_QUOTE_RING_1(29),

    PET_EQUIP_2(30),
    PET_LABEL_RING_2(31),
    PET_QUOTE_RING_2(32),
    PET_ITEM_POUCH_2(33),
    PET_MESO_MAGNET_2(34),
    PET_WING_BOOTS_2(35),
    PET_BINOCULAR_2(36),
    PET_MAGIC_SCALES_2(37),

    PET_EQUIP_3(38),
    PET_LABEL_RING_3(39),
    PET_QUOTE_RING_3(40),
    PET_ITEM_POUCH_3(41),
    PET_MESO_MAGNET_3(42),
    PET_WING_BOOTS_3(43),
    PET_BINOCULAR_3(44),
    PET_MAGIC_SCALES_3(45),

    PET_ITEM_IGNORE_1(46),
    PET_ITEM_IGNORE_2(47),
    PET_ITEM_IGNORE_3(48),

    MEDAL(49),
    BELT(50),
    SHOULDER(51),
    BADGE(56),
    PENDANT_EXT(61),
    EQUIPPED_END(60),

    CASH_BASE(100), // -100 when encoding
    CASH_WEAPON(111),
    CASH_END(160),

    DRAGON_BASE(1000),
    DRAGON_MASK(1000),
    DRAGON_PENDANT(1001),
    DRAGON_WING(1002),
    DRAGON_TAIL(1003),
    DRAGON_END(1100),

    MECHANIC_BASE(1100),
    MECHANIC_ENGINE(1100),
    MECHANIC_ARM(1101),
    MECHANIC_LEG(1102),
    MECHANIC_FRAME(1103),
    MECHANIC_TRANSISTOR(1104),
    MECHANIC_END(1200);

    private final int value;

    BodyPart(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }

    public static BodyPart getBySelectedAL(int i) {
        if (i == 4) {
            return COAT;
        } else if (i == 5) {
            return PANTS;
        } else if (i == 6) {
            return SHOES;
        } else if (i == 7) {
            return WEAPON;
        }
        throw new IllegalArgumentException();
    }
}
