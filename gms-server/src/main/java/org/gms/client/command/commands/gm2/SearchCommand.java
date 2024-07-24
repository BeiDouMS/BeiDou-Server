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
package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.id.NpcId;
import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.ItemInformationProvider;
import org.gms.server.quest.Quest;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;

public class SearchCommand extends Command {
    private static Data npcStringData;
    private static Data mobStringData;
    private static Data skillStringData;
    private static Data mapStringData;

    {
        setDescription(I18nUtil.getMessage("SearchCommand.message1"));

        DataProvider dataProvider = DataProviderFactory.getDataProvider(WZFiles.STRING);
        npcStringData = dataProvider.getData("Npc.img");
        mobStringData = dataProvider.getData("Mob.img");
        skillStringData = dataProvider.getData("Skill.img");
        mapStringData = dataProvider.getData("Map.img");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 2) {
            player.yellowMessage(I18nUtil.getMessage("SearchCommand.message2"));
            return;
        }
        StringBuilder sb = new StringBuilder();

        String search = joinStringFrom(params, 1);
        long start = System.currentTimeMillis();//for the lulz
        Data data = null;
        int searchType = 0;

        switch (params[0].toUpperCase()) {
            case "ITEM":
            case "物品":
                searchType = -1;
                break;
            case "NPC":
                data = npcStringData;
                break;
            case "MOB":
            case "MONSTER":
            case "怪物":
                data = mobStringData;
                break;
            case "SKILL":
            case "技能":
                data = skillStringData;
                break;
            case "MAP":
            case "地图":
                data = mapStringData;
                searchType = 1;
                break;
            case "QUEST":
            case "任务":
                data = mapStringData;
                searchType = 2;
                break;
            default:
                sb.append("#b").append(I18nUtil.getMessage("SearchCommand.message3")).append("\r\n").append(I18nUtil.getMessage("SearchCommand.message4"));
                break;
        }

        if (data != null) {
            String name;
            if (searchType == 0) {
                for (Data searchData : data.getChildren()) {
                    name = DataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                    if (name.toLowerCase().contains(search.toLowerCase())) {
                        sb.append("#b").append(Integer.parseInt(searchData.getName())).append("#k - #r").append(name).append("\r\n");
                    }
                }
            } else if (searchType == 1) {
                String mapName, streetName;

                for (Data searchDataDir : data.getChildren()) {
                    for (Data searchData : searchDataDir.getChildren()) {
                        mapName = DataTool.getString(searchData.getChildByPath("mapName"), "NO-NAME");
                        streetName = DataTool.getString(searchData.getChildByPath("streetName"), "NO-NAME");

                        if (mapName.toLowerCase().contains(search.toLowerCase()) || streetName.toLowerCase().contains(search.toLowerCase())) {
                            sb.append("#b").append(Integer.parseInt(searchData.getName())).append("#k - #r").append(streetName).append(" - ").append(mapName).append("\r\n");
                        }
                    }
                }
            } else {
                for (Quest mq : Quest.getMatchedQuests(search)) {
                    sb.append("#b").append(mq.getId()).append("#k - #r");

                    String parentName = mq.getParentName();
                    if (!parentName.isEmpty()) {
                        sb.append(parentName).append(" - ");
                    }
                    sb.append(mq.getName()).append("\r\n");
                }
            }
        } else if (searchType == -1) {
            for (Pair<Integer, String> itemPair : ItemInformationProvider.getInstance().getAllItems()) {
                if (sb.length() < 32654) {//ohlol
                    if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                        sb.append("#b").append(itemPair.getLeft()).append("#k - #r").append(itemPair.getRight()).append("\r\n");
                    }
                } else {
                    sb.append("#b").append(I18nUtil.getMessage("SearchCommand.message5")).append("\r\n");
                    break;
                }
            }
        }
        if (sb.isEmpty()) {
            sb.append("#b").append(I18nUtil.getMessage("SearchCommand.message6", params[0])).append("\r\n");
        }
        sb.append("\r\n#k").append(I18nUtil.getMessage("SearchCommand.message7", (System.currentTimeMillis() - start) / 1000D));//because I can, and it's free

        c.getAbstractPlayerInteraction().npcTalk(NpcId.MAPLE_ADMINISTRATOR, sb.toString());
    }
}
