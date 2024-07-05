package org.gms.client.command.commands.gm0;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.util.I18nUtil;

public class ReadPointsCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ReadPointsCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {

        Character player = client.getPlayer();
        if (params.length > 2) {
            player.yellowMessage(I18nUtil.getMessage("ReadPointsCommand.message2"));
            return;
        } else if (params.length == 0) {
            player.yellowMessage(I18nUtil.getMessage("ReadPointsCommand.message3") + player.getRewardPoints() + " | "
                    + I18nUtil.getMessage("ReadPointsCommand.message4") + player.getClient().getVotePoints());
            return;
        }

        switch (params[0]) {
            case "rp":
                player.yellowMessage(I18nUtil.getMessage("ReadPointsCommand.message3") + player.getRewardPoints());
                break;
            case "vp":
                player.yellowMessage(I18nUtil.getMessage("ReadPointsCommand.message4") + player.getClient().getVotePoints());
                break;
            default:
                player.yellowMessage(I18nUtil.getMessage("ReadPointsCommand.message3") + player.getRewardPoints() + " | "
                        + I18nUtil.getMessage("ReadPointsCommand.message4") + player.getClient().getVotePoints());
                break;
        }
    }
}