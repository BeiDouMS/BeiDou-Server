/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm3;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Disease;
import org.gms.client.command.Command;
import org.gms.server.life.MobSkill;
import org.gms.server.life.MobSkillFactory;
import org.gms.server.life.MobSkillType;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.util.I18nUtil;

import java.util.Arrays;
import java.util.Optional;

public class DebuffCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("DebuffCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("DebuffCommand.message2"));
            return;
        }

        Disease disease = null;
        Optional<MobSkill> skill = Optional.empty();
        String param = params[0].toUpperCase();
        String skillSlow = I18nUtil.getMessage("DebuffCommand.skill.slow");
        String skillSeduce = I18nUtil.getMessage("DebuffCommand.skill.seduce");
        String skillZombify = I18nUtil.getMessage("DebuffCommand.skill.zombify");
        String skillConfuse = I18nUtil.getMessage("DebuffCommand.skill.confuse");
        String skillStun = I18nUtil.getMessage("DebuffCommand.skill.stun");
        String skillPoison = I18nUtil.getMessage("DebuffCommand.skill.poison");
        String skillSeal = I18nUtil.getMessage("DebuffCommand.skill.seal");
        String skillDarkness = I18nUtil.getMessage("DebuffCommand.skill.darkness");
        String skillWeaken = I18nUtil.getMessage("DebuffCommand.skill.weaken");
        String skillCurse = I18nUtil.getMessage("DebuffCommand.skill.curse");

        if (param.equals(skillSlow)) {
            disease = Disease.SLOW;
            skill = MobSkillFactory.getMobSkill(MobSkillType.SLOW, 7);
        } else if (param.equals(skillSeduce)) {
            disease = Disease.SEDUCE;
            skill = MobSkillFactory.getMobSkill(MobSkillType.SEDUCE, 7);
        } else if (param.equals(skillZombify)) {
            disease = Disease.ZOMBIFY;
            skill = MobSkillFactory.getMobSkill(MobSkillType.UNDEAD, 1);
        } else if (param.equals(skillConfuse)) {
            disease = Disease.CONFUSE;
            skill = MobSkillFactory.getMobSkill(MobSkillType.REVERSE_INPUT, 2);
        } else if (param.equals(skillStun)) {
            disease = Disease.STUN;
            skill = MobSkillFactory.getMobSkill(MobSkillType.STUN, 7);
        } else if (param.equals(skillPoison)) {
            disease = Disease.POISON;
            skill = MobSkillFactory.getMobSkill(MobSkillType.POISON, 5);
        } else if (param.equals(skillSeal)) {
            disease = Disease.SEAL;
            skill = MobSkillFactory.getMobSkill(MobSkillType.SEAL, 1);
        } else if (param.equals(skillDarkness)) {
            disease = Disease.DARKNESS;
            skill = MobSkillFactory.getMobSkill(MobSkillType.DARKNESS, 1);
        } else if (param.equals(skillWeaken)) {
            disease = Disease.WEAKEN;
            skill = MobSkillFactory.getMobSkill(MobSkillType.WEAKNESS, 1);
        } else if (param.equals(skillCurse)) {
            disease = Disease.CURSE;
            skill = MobSkillFactory.getMobSkill(MobSkillType.CURSE, 1);
        }

        if (disease == null || skill.isEmpty()) {
            player.yellowMessage(I18nUtil.getMessage("DebuffCommand.message2"));
            return;
        }

        for (MapObject mmo : player.getMap().getMapObjectsInRange(player.getPosition(), 777777.7, Arrays.asList(MapObjectType.PLAYER))) {
            Character chr = (Character) mmo;

            if (chr.getId() != player.getId()) {
                chr.giveDebuff(disease, skill.get());
            }
        }
    }
}
