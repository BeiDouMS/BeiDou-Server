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
import org.gms.dao.entity.GachaponRewardDO;
import org.gms.manager.ServerManager;
import org.gms.server.gachapon.Gachapon;
import org.gms.service.GachaponService;
import org.gms.util.I18nUtil;

import java.util.List;

public class GachaCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("GachaCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Gachapon.GachaponType gacha = null;
        String search = c.getPlayer().getLastCommandMessage();
        String gachaName = "";
        String[] names = Gachapon.GachaponType.getLootNames();
        int[] ids = Gachapon.GachaponType.getLootIds();
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
        StringBuilder talkStr = new StringBuilder("#b" + gachaName + "#k");
        talkStr.append(I18nUtil.getMessage("GachaCommand.message13"));
        talkStr.append("\r\n\r\n");
        GachaponService gachaponService = ServerManager.getApplicationContext().getBean(GachaponService.class);
        List<GachaponRewardDO> gachaponRewardDOS = gachaponService.getRewardsByNpcId(gacha.getNpcId());
        for (GachaponRewardDO gachaponRewardDO : gachaponRewardDOS) {
            talkStr.append("#v").append(gachaponRewardDO.getItemId()).append("#   -  #z").append(gachaponRewardDO.getItemId()).append("#\r\n");
        }
        talkStr.append("\r\n");
        talkStr.append(I18nUtil.getMessage("GachaCommand.message14"));

        c.getAbstractPlayerInteraction().npcTalk(NpcId.MAPLE_ADMINISTRATOR, talkStr.toString());
    }
}
