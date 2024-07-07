package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import server.maps.MiniDungeonInfo;

public class FmCommand extends Command {
    {
        setDescription("传送到自由市场");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        try {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt("910000000"));
            if (target == null) {
                player.yellowMessage("找不到地图ID");
                return;
            }

            if (!player.isAlive()) {
                player.dropMessage(1, "死亡的时候无法传送。");
                return;
            }

            if (!player.isGM()) {
                if (player.getEventInstance() != null || MiniDungeonInfo.isDungeonMap(player.getMapId()) || FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
                    player.dropMessage(1, "当前地图无法传送");
                    return;
                }
            }

            if (player.getMapId() == 910000000) {
                player.dropMessage(1, "你已经在自由市场了。");
                return;
            }

            // expedition issue with this command detected thanks to Masterrulax
            player.saveLocation("FREE_MARKET");
            player.changeMap(target, target.findMarketPortal());
        } catch (Exception ex) {
            player.yellowMessage("传送到自由市场失败了。。。");
        }
    }
}
