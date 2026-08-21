/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package org.gms.client.creator.novice;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.client.creator.CharacterFactory;
import org.gms.client.creator.CharacterFactoryRecipe;
import org.gms.client.inventory.InventoryType;
import org.gms.config.GameConfig;
import org.gms.constants.id.ItemId;
import org.gms.constants.id.MapId;

/**
 * @author RonanLana
 */
public class BeginnerCreator extends CharacterFactory {

    public static final int DEFAULT_GENDER = 0;
    public static final int DEFAULT_FACE = 20000;
    public static final int DEFAULT_HAIR = 30000;
    public static final int DEFAULT_SKIN = 0;
    public static final int DEFAULT_TOP = 1040002;
    public static final int DEFAULT_BOTTOM = 1060002;
    public static final int DEFAULT_SHOES = 1072001;
    public static final int DEFAULT_WEAPON = 1302000;

    private static CharacterFactoryRecipe createRecipe(Job job, int level, int map, int top, int bottom, int shoes, int weapon) {
        CharacterFactoryRecipe recipe = new CharacterFactoryRecipe(job, level, map, top, bottom, shoes, weapon);
        giveItem(recipe, ItemId.BEGINNERS_GUIDE, 1, InventoryType.ETC);
        return recipe;
    }

    private static void giveItem(CharacterFactoryRecipe recipe, int itemid, int quantity, InventoryType itemType) {
        recipe.addStartingItem(itemid, quantity, itemType);
    }

    public static int createCharacter(Client c, String name, int face, int hair, int skin, int top, int bottom, int shoes, int weapon, int gender) {
        return createNewCharacter(c, name, face, hair, skin, gender,
                createRecipe(Job.BEGINNER, 1, beginnerMap(), top, bottom, shoes, weapon));
    }

    public static CharacterFactoryRecipe createDefaultRecipe() {
        return createRecipe(Job.BEGINNER, 1, beginnerMap(),
                DEFAULT_TOP, DEFAULT_BOTTOM, DEFAULT_SHOES, DEFAULT_WEAPON);
    }

    public static Character prepareProvisionedCharacter(int accountId, int worldId, String name) {
        Client client = Client.createMock();
        client.setAccID(accountId);
        client.setWorld(worldId);
        client.setAccountName("<provisioning>");
        return prepareNewCharacter(client, name, DEFAULT_FACE, DEFAULT_HAIR,
                DEFAULT_SKIN, DEFAULT_GENDER, createDefaultRecipe());
    }

    private static int beginnerMap() {
        return GameConfig.getServerBoolean("use_beidou_beginner_map")
                ? MapId.BEIDOU_BEGINNER
                : MapId.MUSHROOM_TOWN;
    }
}
