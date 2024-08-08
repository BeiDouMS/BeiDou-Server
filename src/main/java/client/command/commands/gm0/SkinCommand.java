package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class SkinCommand extends Command {
    {
        setDescription("设置伤害皮肤");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(NpcId.MAPLE_ADMINISTRATOR, "伤害皮肤");
    }
}
