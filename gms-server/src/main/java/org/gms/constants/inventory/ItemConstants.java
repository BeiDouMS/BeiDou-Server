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
package org.gms.constants.inventory;

import org.gms.client.inventory.InventoryType;
import org.gms.config.GameConfig;
import org.gms.constants.id.ItemId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jay Estrella
 * @author Ronan
 */
public final class ItemConstants {
    protected static Map<Integer, InventoryType> inventoryTypeCache = new HashMap<>();

    public final static short LOCK = 0x01;
    public final static short SPIKES = 0x02;
    public final static short KARMA_USE = 0x02;
    public final static short COLD = 0x04;
    public final static short UNTRADEABLE = 0x08;
    public final static short KARMA_EQP = 0x10;
    public final static short SANDBOX = 0x40;             // let 0x40 until it's proven something uses this
    public final static short PET_COME = 0x80;
    public final static short ACCOUNT_SHARING = 0x100;
    public final static short MERGE_UNTRADEABLE = 0x200;

    public final static Set<Integer> permanentItemids = new HashSet<>();

    static {
        // i ain't going to open one gigantic itemid cache just for 4 perma itemids, no way!
        for (int petItemId : ItemId.getPermaPets()) {
            permanentItemids.add(petItemId);
        }
    }

    public static int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return ACCOUNT_SHARING;
        }
        return 0;
    }

    public static boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }

    public static boolean isPotion(int itemId) {
        return itemId / 1000 == 2000;
    }

    public static boolean isFood(int itemId) {
        int useType = itemId / 1000;
        return useType == 2022 || useType == 2010 || useType == 2020;
    }

    public static boolean isConsumable(int itemId) {
        return isPotion(itemId) || isFood(itemId);
    }

    public static boolean isRechargeable(int itemId) {
        return isThrowingStar(itemId) || isBullet(itemId);
    }

    public static boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }

    public static boolean isArrow(int itemId) {
        return isArrowForBow(itemId) || isArrowForCrossBow(itemId);
    }

    public static boolean isPet(int itemId) {
        return itemId / 1000 == 5000;
    }

    public static boolean isExpirablePet(int itemId) {
        return GameConfig.getServerBoolean("use_erase_pet_on_expiration") || itemId == ItemId.PET_SNAIL;
    }

    public static boolean isPermanentItem(int itemId) {
        return permanentItemids.contains(itemId);
    }

    public static boolean isNewYearCardEtc(int itemId) {
        return itemId / 10000 == 430;
    }

    public static boolean isNewYearCardUse(int itemId) {
        return itemId / 10000 == 216;
    }

    public static boolean isAccessory(int itemId) {
        return itemId >= 1110000 && itemId < 1140000;
    }

    public static boolean isTaming(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 1902 || itemType == 1912;
    }

    public static boolean isTownScroll(int itemId) {
        return itemId >= 2030000 && itemId < ItemId.ANTI_BANISH_SCROLL;
    }

    public static boolean isAntibanishScroll(int itemId) {
        return itemId == ItemId.ANTI_BANISH_SCROLL;
    }

    public static boolean isCleanSlate(int scrollId) {
        return scrollId > 2048999 && scrollId < 2049004;
    }

    public static boolean isModifierScroll(int scrollId) {
        return scrollId == ItemId.SPIKES_SCROLL || scrollId == ItemId.COLD_PROTECTION_SCROLl;
    }

    public static boolean isFlagModifier(int scrollId, short flag) {
        if (scrollId == ItemId.COLD_PROTECTION_SCROLl && ((flag & ItemConstants.COLD) == ItemConstants.COLD)) {
            return true;
        }
        return scrollId == ItemId.SPIKES_SCROLL && ((flag & ItemConstants.SPIKES) == ItemConstants.SPIKES);
    }

    public static boolean isChaosScroll(int scrollId) {
        return scrollId >= 2049100 && scrollId <= 2049103;
    }

    public static boolean isRateCoupon(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 5211 || itemType == 5360;
    }

    public static boolean isExpCoupon(int couponId) {
        return couponId / 1000 == 5211;
    }

    public static boolean isPartyItem(int itemId) {
        return itemId >= 2022430 && itemId <= 2022433 || itemId >= 2022160 && itemId <= 2022163;
    }

    public static boolean isHiredMerchant(int itemId) {
        return itemId / 10000 == 503;
    }

    public static boolean isPlayerShop(int itemId) {
        return itemId / 10000 == 514;
    }

    public static InventoryType getInventoryType(final int itemId) {
        if (inventoryTypeCache.containsKey(itemId)) {
            return inventoryTypeCache.get(itemId);
        }

        InventoryType ret = InventoryType.UNDEFINED;

        final byte type = (byte) (itemId / 1000000);
        if (type >= 1 && type <= 5) {
            ret = InventoryType.getByType(type);
        }

        inventoryTypeCache.put(itemId, ret);
        return ret;
    }

    public static boolean isMakerReagent(int itemId) {
        return itemId / 10000 == 425;
    }

    public static boolean isOverall(int itemId) {
        return itemId / 10000 == 105;
    }

    public static boolean isCashStore(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 503 || itemType == 514;
    }

    public static boolean isMapleLife(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 543 && itemId != 5430000;
    }

    public static boolean isWeapon(int itemId) {
        return itemId >= 1302000 && itemId < 1493000;
    }

    public static boolean isEquipment(int itemId) {
        return itemId < 2000000 && itemId != 0;
    }

    public static boolean isFishingChair(int itemId) {
        return itemId == ItemId.FISHING_CHAIR;
    }

    public static boolean isMedal(int itemId) {
        return itemId >= 1140000 && itemId < 1143000;
    }

    public static boolean isFace(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 2 || itemType == 5;
    }

    public static boolean isHair(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 3 || itemType == 4 || itemType == 6;
    }

    public static boolean isNewCharDefaultFace(int job, int gender, int faceId) {
        if (job == 0 || job == 1) {
            return switch (gender) {
                case 0 -> faceId == 20000 || faceId == 20001 || faceId == 20002;
                case 1 -> faceId == 21000 || faceId == 21001 || faceId == 21002;
                default -> false;
            };
        } else if (job == 2) {
            return switch (gender) {
                case 0 -> faceId == 20100 || faceId == 20401 || faceId == 20402;
                case 1 -> faceId == 21700 || faceId == 21201 || faceId == 21002;
                default -> false;
            };
        } else {
            return false;
        }
    }

    public static boolean isNewCharDefaultHair(int gender, int hairId) {
        return switch (gender) {
            case 0 -> hairId == 30000 || hairId == 30020 || hairId == 30030;
            case 1 -> hairId == 31000 || hairId == 31040 || hairId == 31050;
            default -> false;
        };
    }

    public static boolean isNewCharDefaultHairColor(int hairColor) {
        return hairColor == 0 || hairColor == 2 || hairColor == 3 || hairColor == 7;
    }

    public static boolean isNewCharDefaultSkinColor(int skinColor) {
        return skinColor >= 0 && skinColor < 4;
    }

    public static boolean isNewCharDefaultTop(int job, int gender, int topId) {
        if (job == 0 || job == 1) {
            return switch (gender) {
                case 0 -> topId == 1040002 || topId == 1040006 || topId == 1040010;
                case 1 -> topId == 1041002 || topId == 1041006 || topId == 1041010 || topId == 1041011;
                default -> false;
            };
        } else if (job == 2) {
            return topId == 1042167;
        } else {
            return false;
        }
    }

    public static boolean isNewCharDefaultBottom(int job, int gender, int bottomId) {
        if (job == 0 || job == 1) {
            return switch (gender) {
                case 0 -> bottomId == 1060002 || bottomId == 1060006;
                case 1 -> bottomId == 1061002 || bottomId == 1061008;
                default -> false;
            };
        } else if (job == 2) {
            return bottomId == 1062115;
        } else {
            return false;
        }
    }

    public static boolean isNewCharDefaultShoes(int job, int shoesId) {
        if (job == 0 || job == 1) {
            return shoesId == 1072001 || shoesId == 1072005 || shoesId == 1072037 || shoesId == 1072038;
        } else if (job == 2) {
            return shoesId == 1072383;
        } else {
            return false;
        }
    }

    public static boolean isNewCharDefaultWeapon(int job, int weaponId) {
        if (job == 0 || job == 1) {
            return weaponId == 1302000 || weaponId == 1322005 || weaponId == 1312004;
        } else if (job == 2) {
            return weaponId == 1442079;
        } else {
            return false;
        }
    }

    public static boolean notValidHairColor(int hairColor) {
        return hairColor > 7 || hairColor < 0;
    }
}
