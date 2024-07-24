package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.life.MobSkill;
import org.gms.server.life.MobSkillFactory;
import org.gms.server.life.MobSkillType;
import org.gms.util.I18nUtil;

import java.util.Collections;
import java.util.Optional;

public class MobSkillCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("MobSkillCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {
        Character chr = client.getPlayer();
        if (params.length < 2) {
            chr.message(I18nUtil.getMessage("MobSkillCommand.message2"));
            return;
        }

        String skillId = params[0];
        String skillLevel = params[1];
        Optional<MobSkillType> possibleType = MobSkillType.from(Integer.parseInt(skillId));
        Optional<MobSkill> possibleSkill = possibleType.map(
                type -> MobSkillFactory.getMobSkillOrThrow(type, Integer.parseInt(skillLevel))
        );
        if (possibleSkill.isEmpty()) {
            return;
        }

        MobSkill mobSkill = possibleSkill.get();
        chr.getMap().getAllMonsters().forEach(
                monster -> mobSkill.applyEffect(chr, monster, false, Collections.emptyList())
        );
    }
}
