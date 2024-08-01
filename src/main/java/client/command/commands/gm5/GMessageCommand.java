package client.command.commands.gm5;

import client.Character;
import client.Client;
import client.command.Command;
import tools.PacketCreator;

public class GMessageCommand extends Command {
    {
        setDescription("发送 G_Message");
    }

    @Override
    public void execute(Client c, String[] params) {
        if (params.length < 4) {
            c.getPlayer().dropMessage(5, "使用语法 @gmsg <玩家名字> <颜色:0红,1绿,2蓝> <时间/秒, 0常驻用户可主动关闭, 或当消息队列满时被清除> <发送的消息>");
        } else {
            Character character = c.getChannelServer().getPlayerStorage().getCharacterByName(params[0]);
            if (character == null) {
                c.getPlayer().message("找不到玩家：" + params[0]);
                return;
            }

            character.sendPacket(PacketCreator.gMessage(Byte.parseByte(params[1]), params[3], Byte.parseByte(params[2])));
        }
    }
}
