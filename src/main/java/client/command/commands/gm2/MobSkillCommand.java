package client.command.commands.gm2;

import client.Character;
import client.Client;
import client.command.Command;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.MobSkillType;

import java.util.Collections;

public class MobSkillCommand extends Command {
    {
        setDescription("Apply a mob skill to all mobs on the map. Args: <mob skill id> <skill level>");
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length < 2) {
            throw new IllegalArgumentException("Mob skill command requires two args: mob skill id and level");
        }

        String skillId = params[0];
        String skillLevel = params[1];
        MobSkillType type = MobSkillType.from(Integer.parseInt(skillId));
        MobSkill mobSkill = MobSkillFactory.getMobSkill(type, Integer.parseInt(skillLevel));
        if (mobSkill == null) {
            throw new IllegalArgumentException("Mob skill not found. Id: %s, level: %s".formatted(skillId, skillLevel));
        }

        Character chr = client.getPlayer();
        chr.getMap().getAllMonsters().forEach(monster -> mobSkill.applyEffect(chr, monster, false, Collections.emptyList()));
    }
}
