package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import tools.PacketCreator;

public class UICommand extends Command {
    {
        setDescription("显示UI");
    }

    @Override
    public void execute(Client c, String[] params) {
        if (params.length == 0) {
            c.getPlayer().dropMessage(5, "请输入参数：");
            c.getPlayer().dropMessage(5, "统计");
            c.getPlayer().dropMessage(5, "boss统计");
        } else if (params[0].equals("boss统计")) {
            c.getPlayer().sendPacket(PacketCreator.showUI((byte) 1));
        } else if (params[0].equals("统计")) {
            c.getPlayer().sendPacket(PacketCreator.showUI((byte) 2));
        }
    }
}
