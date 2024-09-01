package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import tools.PacketCreator;

public class UICommand extends Command {
    {
        setDescription("显示CGui界面");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getPlayer().sendPacket(PacketCreator.openCGui());
    }
}
