package client.command.commands.gm1;

import client.Character;
import client.Client;
import client.command.Command;
import config.YamlConfig;

public class MobRateCommand extends Command {
    {
        setDescription("设置地图怪物倍率");
    }

    @Override
    public void execute(Client c, String[] params) {
        if (c.tryacquireClient()) {
            try {
                Character chr = c.getPlayer();

                if (params.length == 0) {
                    int fighter = chr.getMap().countFightingPlayer();
                    chr.yellowMessage("战斗人员：" + fighter + " 人");
                    chr.yellowMessage("怪物倍率： " + chr.getMap().getCurrentSpawnRate(fighter) * 100 + "%");
                    if (YamlConfig.config.server.RESPAWN_FORCE_MAX_FIGHTER) {
                        chr.yellowMessage("已开启强制满人倍率");
                    }
                    if (YamlConfig.config.server.RESPAWN_CUSTOM_RATE && chr.getMap().getMobRate() > 0) {
                        chr.yellowMessage("已开启自定义倍率");
                    }
                    chr.yellowMessage("怪物总数：" + chr.getMap().countMonsterSpawn());
                    chr.yellowMessage("当前怪物：" + chr.getMap().getSpawnedMonstersOnMap());
                    return;
                }

                if (!YamlConfig.config.server.RESPAWN_CUSTOM_RATE) {
                    chr.message("服务器未开启自定义倍率功能");
                    return;
                }


                double rate = Math.round(Double.parseDouble(params[0]) * 100.0) / 100.0;

                if (rate <= 0) {
                    chr.getMap().setMobRate(0);
                    chr.dropMessage("已关闭自定义倍率");
                    return;
                }

                chr.getMap().setMobRate(rate);
                chr.dropMessage("已开启自定义倍率，当前地图怪物倍率为: " + rate * 100 + "%");
            } finally {
                c.releaseClient();
            }
        }
    }
}
