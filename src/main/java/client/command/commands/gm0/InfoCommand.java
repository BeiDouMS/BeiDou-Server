package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class InfoCommand extends Command {
    {
        setDescription("显示玩家信息");
    }

    @Override
    public void execute(Client client, String[] params) {
        client.getAbstractPlayerInteraction().openNpc(NpcId.MAPLE_ADMINISTRATOR, "info");
    }
}
