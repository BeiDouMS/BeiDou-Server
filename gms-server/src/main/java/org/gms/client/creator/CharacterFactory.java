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
package org.gms.client.creator;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.SkinColor;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.config.GameConfig;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.ItemInformationProvider;
import org.gms.util.PacketCreator;

/**
 * @author RonanLana
 */
public abstract class CharacterFactory {
    private static final Logger log = LoggerFactory.getLogger(CharacterFactory.class);

    protected synchronized static int createNewCharacter(Client c, String name, int face, int hair, int skin, int gender, CharacterFactoryRecipe recipe) {
        if (GameConfig.getServerBoolean("collective_chr_slot") ? c.getAvailableCharacterSlots() <= 0 : c.getAvailableCharacterWorldSlots() <= 0) {
            return -3;
        }

        if (!Character.canCreateChar(name)) {
            return -1;
        }

        Character newCharacter = Character.getDefault(c);
        newCharacter.setWorld(c.getWorld());
        newCharacter.setSkinColor(SkinColor.getById(skin));
        newCharacter.setGender(gender);
        newCharacter.setName(name);
        newCharacter.setHair(hair);
        newCharacter.setFace(face);

        newCharacter.setLevel(recipe.getLevel());
        newCharacter.setJob(recipe.getJob());
        newCharacter.setMapId(recipe.getMap());

        Inventory equipped = newCharacter.getInventory(InventoryType.EQUIPPED);
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        int top = recipe.getTop(), bottom = recipe.getBottom(), shoes = recipe.getShoes(), weapon = recipe.getWeapon();

        if (top > 0) {
            Item eq_top = ii.getEquipById(top);
            eq_top.setPosition((byte) -5);
            equipped.addItemFromDB(eq_top);
        }

        if (bottom > 0) {
            Item eq_bottom = ii.getEquipById(bottom);
            eq_bottom.setPosition((byte) -6);
            equipped.addItemFromDB(eq_bottom);
        }

        if (shoes > 0) {
            Item eq_shoes = ii.getEquipById(shoes);
            eq_shoes.setPosition((byte) -7);
            equipped.addItemFromDB(eq_shoes);
        }

        if (weapon > 0) {
            Item eq_weapon = ii.getEquipById(weapon);
            eq_weapon.setPosition((byte) -11);
            equipped.addItemFromDB(eq_weapon.copy());
        }

        if (!MakeCharInfoValidator.isNewCharacterValid(newCharacter)) {
            log.warn("Owner from account {} tried to packet edit in character creation", c.getAccountName());
            return -2;
        }

        if (!newCharacter.insertNewChar(recipe)) {
            return -2;
        }
        c.sendPacket(PacketCreator.addNewCharEntry(newCharacter));

        Server.getInstance().createCharacterEntry(newCharacter);
        Server.getInstance().broadcastGMMessage(c.getWorld(), PacketCreator.sendYellowTip("[New Char]: " + c.getAccountName() + I18nUtil.getMessage("CharacterFactory.message1") + name));
        log.info("账号 {} 创建了角色 {}", c.getAccountName(), name);

        return 0;
    }
}
