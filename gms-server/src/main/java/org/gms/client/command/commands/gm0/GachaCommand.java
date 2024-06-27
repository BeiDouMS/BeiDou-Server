/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm0;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.id.NpcId;
import org.gms.server.ItemInformationProvider;
import org.gms.server.gachapon.Gachapon;
import org.gms.util.I18nUtil;

public class GachaCommand extends Command {
    {
        setDescription(I18nUtil.getLogMessage("GachaCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Gachapon.GachaponType gacha = null;
        String search = c.getPlayer().getLastCommandMessage();
        String gachaName = "";
        String[] names = {I18nUtil.getLogMessage("GachaCommand.message2"),
                I18nUtil.getLogMessage("GachaCommand.message3"),
                I18nUtil.getLogMessage("GachaCommand.message4"),
                I18nUtil.getLogMessage("GachaCommand.message5"),
                I18nUtil.getLogMessage("GachaCommand.message6"),
                I18nUtil.getLogMessage("GachaCommand.message7"),
                I18nUtil.getLogMessage("GachaCommand.message8"),
                I18nUtil.getLogMessage("GachaCommand.message9"),
                I18nUtil.getLogMessage("GachaCommand.message10"),
                I18nUtil.getLogMessage("GachaCommand.message11")};
        int[] ids = {NpcId.GACHAPON_HENESYS, NpcId.GACHAPON_ELLINIA, NpcId.GACHAPON_PERION, NpcId.GACHAPON_KERNING,
                NpcId.GACHAPON_SLEEPYWOOD, NpcId.GACHAPON_MUSHROOM_SHRINE, NpcId.GACHAPON_SHOWA_MALE,
                NpcId.GACHAPON_SHOWA_FEMALE, NpcId.GACHAPON_NLC, NpcId.GACHAPON_NAUTILUS};
        for (int i = 0; i < names.length; i++) {
            if (search.equalsIgnoreCase(names[i])) {
                gachaName = names[i];
                gacha = Gachapon.GachaponType.getByNpcId(ids[i]);
            }
        }
        if (gacha == null) {
            c.getPlayer().yellowMessage(I18nUtil.getMessage("GachaCommand.message12"));
            for (String name : names) {
                c.getPlayer().yellowMessage(name);
            }
            return;
        }
        String talkStr = "#b" + gachaName + "#k";
        talkStr += I18nUtil.getMessage("GachaCommand.message13");
        talkStr += "\r\n\r\n";
        for (int i = 0; i < 2; i++) {
            for (int id : gacha.getItems(i)) {
                talkStr += "-" + ItemInformationProvider.getInstance().getName(id) + "\r\n";
            }
        }
        talkStr += "\r\n";
        talkStr += I18nUtil.getMessage("GachaCommand.message14");

        c.getAbstractPlayerInteraction().npcTalk(NpcId.MAPLE_ADMINISTRATOR, talkStr);
    }
}
