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
package org.gms.client.command;

import lombok.Getter;
import org.gms.client.Client;
import org.gms.client.command.commands.gm0.ChangeLanguageCommand;
import org.gms.client.command.commands.gm0.DisposeCommand;
import org.gms.client.command.commands.gm0.DropLimitCommand;
import org.gms.client.command.commands.gm0.EnableAuthCommand;
import org.gms.client.command.commands.gm0.EquipLvCommand;
import org.gms.client.command.commands.gm0.GachaCommand;
import org.gms.client.command.commands.gm0.GmCommand;
import org.gms.client.command.commands.gm0.HelpCommand;
import org.gms.client.command.commands.gm0.JoinEventCommand;
import org.gms.client.command.commands.gm0.LeaveEventCommand;
import org.gms.client.command.commands.gm0.MapOwnerClaimCommand;
import org.gms.client.command.commands.gm0.OnlineCommand;
import org.gms.client.command.commands.gm0.RanksCommand;
import org.gms.client.command.commands.gm0.RatesCommand;
import org.gms.client.command.commands.gm0.ReadPointsCommand;
import org.gms.client.command.commands.gm0.ReportBugCommand;
import org.gms.client.command.commands.gm0.ShowRatesCommand;
import org.gms.client.command.commands.gm0.StaffCommand;
import org.gms.client.command.commands.gm0.StatDexCommand;
import org.gms.client.command.commands.gm0.StatIntCommand;
import org.gms.client.command.commands.gm0.StatLukCommand;
import org.gms.client.command.commands.gm0.StatStrCommand;
import org.gms.client.command.commands.gm0.TimeCommand;
import org.gms.client.command.commands.gm0.ToggleExpCommand;
import org.gms.client.command.commands.gm0.UptimeCommand;
import org.gms.client.command.commands.gm1.BossHpCommand;
import org.gms.client.command.commands.gm1.BuffMeCommand;
import org.gms.client.command.commands.gm1.GotoCommand;
import org.gms.client.command.commands.gm1.MobHpCommand;
import org.gms.client.command.commands.gm1.WhatDropsFromCommand;
import org.gms.client.command.commands.gm1.WhoDropsCommand;
import org.gms.client.command.commands.gm2.ApCommand;
import org.gms.client.command.commands.gm2.BombCommand;
import org.gms.client.command.commands.gm2.BuffCommand;
import org.gms.client.command.commands.gm2.BuffMapCommand;
import org.gms.client.command.commands.gm2.ClearDropsCommand;
import org.gms.client.command.commands.gm2.ClearSavedLocationsCommand;
import org.gms.client.command.commands.gm2.ClearSlotCommand;
import org.gms.client.command.commands.gm2.DcCommand;
import org.gms.client.command.commands.gm2.EmpowerMeCommand;
import org.gms.client.command.commands.gm2.GachaListCommand;
import org.gms.client.command.commands.gm2.GmShopCommand;
import org.gms.client.command.commands.gm2.HealCommand;
import org.gms.client.command.commands.gm2.HideCommand;
import org.gms.client.command.commands.gm2.IdCommand;
import org.gms.client.command.commands.gm2.ItemCommand;
import org.gms.client.command.commands.gm2.ItemDropCommand;
import org.gms.client.command.commands.gm2.JailCommand;
import org.gms.client.command.commands.gm2.JobCommand;
import org.gms.client.command.commands.gm2.LevelCommand;
import org.gms.client.command.commands.gm2.LevelProCommand;
import org.gms.client.command.commands.gm2.LootCommand;
import org.gms.client.command.commands.gm2.MaxSkillCommand;
import org.gms.client.command.commands.gm2.MaxStatCommand;
import org.gms.client.command.commands.gm2.MobSkillCommand;
import org.gms.client.command.commands.gm2.ReachCommand;
import org.gms.client.command.commands.gm2.RechargeCommand;
import org.gms.client.command.commands.gm2.ResetSkillCommand;
import org.gms.client.command.commands.gm2.SearchCommand;
import org.gms.client.command.commands.gm2.SetSlotCommand;
import org.gms.client.command.commands.gm2.SetStatCommand;
import org.gms.client.command.commands.gm2.SpCommand;
import org.gms.client.command.commands.gm2.SummonCommand;
import org.gms.client.command.commands.gm2.UnBugCommand;
import org.gms.client.command.commands.gm2.UnHideCommand;
import org.gms.client.command.commands.gm2.UnJailCommand;
import org.gms.client.command.commands.gm2.WarpAreaCommand;
import org.gms.client.command.commands.gm2.WarpCommand;
import org.gms.client.command.commands.gm2.WarpMapCommand;
import org.gms.client.command.commands.gm2.WhereaMiCommand;
import org.gms.client.command.commands.gm3.BanCommand;
import org.gms.client.command.commands.gm3.ChatCommand;
import org.gms.client.command.commands.gm3.CheckDmgCommand;
import org.gms.client.command.commands.gm3.ClosePortalCommand;
import org.gms.client.command.commands.gm3.DebuffCommand;
import org.gms.client.command.commands.gm3.EndEventCommand;
import org.gms.client.command.commands.gm3.ExpedsCommand;
import org.gms.client.command.commands.gm3.FaceCommand;
import org.gms.client.command.commands.gm3.FameCommand;
import org.gms.client.command.commands.gm3.FlyCommand;
import org.gms.client.command.commands.gm3.GiveMesosCommand;
import org.gms.client.command.commands.gm3.GiveNxCommand;
import org.gms.client.command.commands.gm3.GiveRpCommand;
import org.gms.client.command.commands.gm3.GiveVpCommand;
import org.gms.client.command.commands.gm3.HairCommand;
import org.gms.client.command.commands.gm3.HealMapCommand;
import org.gms.client.command.commands.gm3.HealPersonCommand;
import org.gms.client.command.commands.gm3.HpMpCommand;
import org.gms.client.command.commands.gm3.HurtCommand;
import org.gms.client.command.commands.gm3.IgnoreCommand;
import org.gms.client.command.commands.gm3.IgnoredCommand;
import org.gms.client.command.commands.gm3.InMapCommand;
import org.gms.client.command.commands.gm3.KillAllCommand;
import org.gms.client.command.commands.gm3.KillCommand;
import org.gms.client.command.commands.gm3.KillMapCommand;
import org.gms.client.command.commands.gm3.MaxEnergyCommand;
import org.gms.client.command.commands.gm3.MaxHpMpCommand;
import org.gms.client.command.commands.gm3.MonitorCommand;
import org.gms.client.command.commands.gm3.MonitorsCommand;
import org.gms.client.command.commands.gm3.MusicCommand;
import org.gms.client.command.commands.gm3.MuteMapCommand;
import org.gms.client.command.commands.gm3.NightCommand;
import org.gms.client.command.commands.gm3.NoticeCommand;
import org.gms.client.command.commands.gm3.NpcCommand;
import org.gms.client.command.commands.gm3.OnlineTwoCommand;
import org.gms.client.command.commands.gm3.OpenPortalCommand;
import org.gms.client.command.commands.gm3.PeCommand;
import org.gms.client.command.commands.gm3.PosCommand;
import org.gms.client.command.commands.gm3.QuestCompleteCommand;
import org.gms.client.command.commands.gm3.QuestResetCommand;
import org.gms.client.command.commands.gm3.QuestStartCommand;
import org.gms.client.command.commands.gm3.ReloadDropsCommand;
import org.gms.client.command.commands.gm3.ReloadEventsCommand;
import org.gms.client.command.commands.gm3.ReloadMapCommand;
import org.gms.client.command.commands.gm3.ReloadPortalsCommand;
import org.gms.client.command.commands.gm3.ReloadShopsCommand;
import org.gms.client.command.commands.gm3.RipCommand;
import org.gms.client.command.commands.gm3.SeedCommand;
import org.gms.client.command.commands.gm3.SpawnCommand;
import org.gms.client.command.commands.gm3.StartEventCommand;
import org.gms.client.command.commands.gm3.StartMapEventCommand;
import org.gms.client.command.commands.gm3.StopMapEventCommand;
import org.gms.client.command.commands.gm3.TimerAllCommand;
import org.gms.client.command.commands.gm3.TimerCommand;
import org.gms.client.command.commands.gm3.TimerMapCommand;
import org.gms.client.command.commands.gm3.ToggleCouponCommand;
import org.gms.client.command.commands.gm3.UnBanCommand;
import org.gms.client.command.commands.gm4.BossDropRateCommand;
import org.gms.client.command.commands.gm4.CakeCommand;
import org.gms.client.command.commands.gm4.DropRateCommand;
import org.gms.client.command.commands.gm4.ExpRateCommand;
import org.gms.client.command.commands.gm4.FishingRateCommand;
import org.gms.client.command.commands.gm4.ForceVacCommand;
import org.gms.client.command.commands.gm4.HorntailCommand;
import org.gms.client.command.commands.gm4.ItemVacCommand;
import org.gms.client.command.commands.gm4.MesoRateCommand;
import org.gms.client.command.commands.gm4.PapCommand;
import org.gms.client.command.commands.gm4.PianusCommand;
import org.gms.client.command.commands.gm4.PinkbeanCommand;
import org.gms.client.command.commands.gm4.PlayerNpcCommand;
import org.gms.client.command.commands.gm4.PlayerNpcRemoveCommand;
import org.gms.client.command.commands.gm4.PmobCommand;
import org.gms.client.command.commands.gm4.PmobRemoveCommand;
import org.gms.client.command.commands.gm4.PnpcCommand;
import org.gms.client.command.commands.gm4.PnpcRemoveCommand;
import org.gms.client.command.commands.gm4.ProItemCommand;
import org.gms.client.command.commands.gm4.QuestRateCommand;
import org.gms.client.command.commands.gm4.ServerMessageCommand;
import org.gms.client.command.commands.gm4.SetEqStatCommand;
import org.gms.client.command.commands.gm4.TravelRateCommand;
import org.gms.client.command.commands.gm4.ZakumCommand;
import org.gms.client.command.commands.gm5.DebugCommand;
import org.gms.client.command.commands.gm5.IpListCommand;
import org.gms.client.command.commands.gm5.SetCommand;
import org.gms.client.command.commands.gm5.ShowMoveLifeCommand;
import org.gms.client.command.commands.gm5.ShowPacketsCommand;
import org.gms.client.command.commands.gm5.ShowSessionsCommand;
import org.gms.client.command.commands.gm6.ClearQuestCacheCommand;
import org.gms.client.command.commands.gm6.ClearQuestCommand;
import org.gms.client.command.commands.gm6.DCAllCommand;
import org.gms.client.command.commands.gm6.DevtestCommand;
import org.gms.client.command.commands.gm6.EraseAllPNpcsCommand;
import org.gms.client.command.commands.gm6.GetAccCommand;
import org.gms.client.command.commands.gm6.MapPlayersCommand;
import org.gms.client.command.commands.gm6.SaveAllCommand;
import org.gms.client.command.commands.gm6.ServerAddChannelCommand;
import org.gms.client.command.commands.gm6.ServerAddWorldCommand;
import org.gms.client.command.commands.gm6.ServerRemoveChannelCommand;
import org.gms.client.command.commands.gm6.ServerRemoveWorldCommand;
import org.gms.client.command.commands.gm6.SetGmLevelCommand;
import org.gms.client.command.commands.gm6.ShutdownCommand;
import org.gms.client.command.commands.gm6.SpawnAllPNpcsCommand;
import org.gms.client.command.commands.gm6.SupplyRateCouponCommand;
import org.gms.client.command.commands.gm6.WarpWorldCommand;
import org.gms.constants.id.MapId;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandsExecutor {
    private static final Logger log = LoggerFactory.getLogger(CommandsExecutor.class);
    @Getter
    private static final CommandsExecutor instance = new CommandsExecutor();
    private static final char USER_HEADING = '@';
    private static final char GM_HEADING = '!';

    private final HashMap<String, Command> registeredCommands = new HashMap<>();
    private final List<Pair<List<String>, List<String>>> commandsNameDesc = new ArrayList<>();
    private Pair<List<String>, List<String>> levelCommandsCursor;

    public static boolean isCommand(Client client, String content) {
        char heading = content.charAt(0);
        if (client.getPlayer().isGM()) {
            return heading == USER_HEADING || heading == GM_HEADING;
        }
        return heading == USER_HEADING;
    }

    public void loadCommandsExecutor() {
        registeredCommands.clear();
        commandsNameDesc.clear();
        registerLv0Commands();
        registerLv1Commands();
        registerLv2Commands();
        registerLv3Commands();
        registerLv4Commands();
        registerLv5Commands();
        registerLv6Commands();
    }

    public List<Pair<List<String>, List<String>>> getGmCommands() {
        return commandsNameDesc;
    }

    public void handle(Client client, String message) {
        if (client.tryacquireClient()) {
            try {
                handleInternal(client, message);
            } finally {
                client.releaseClient();
            }
        } else {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("CommandsExecutor.handle.message1"));
        }
    }

    private void handleInternal(Client client, String message) {
        if (client.getPlayer().getMapId() == MapId.JAIL && !client.getPlayer().isGM()) {
            client.getPlayer().yellowMessage(I18nUtil.getMessage("CommandsExecutor.handleInternal.message1"));
            return;
        }
        final String splitRegex = "[ ]";
        String[] splitedMessage = message.substring(1).split(splitRegex, 2);
        if (splitedMessage.length < 2) {
            splitedMessage = new String[]{splitedMessage[0], ""};
        }

        client.getPlayer().setLastCommandMessage(splitedMessage[1]);    // thanks Tochi & Nulliphite for noticing string messages being marshalled lowercase
        final String commandName = splitedMessage[0].toLowerCase();
        final String[] lowercaseParams = splitedMessage[1].toLowerCase().split(splitRegex);

        final Command command = registeredCommands.get(commandName);
        if (command == null) {
            client.getPlayer().yellowMessage(I18nUtil.getMessage("CommandsExecutor.handleInternal.message2", commandName));
            return;
        }
        if (client.getPlayer().gmLevel() < command.getRank()) {
            client.getPlayer().yellowMessage(I18nUtil.getMessage("CommandsExecutor.handleInternal.message3"));
            return;
        }
        String[] params;
        if (lowercaseParams.length > 0 && !lowercaseParams[0].isEmpty()) {
            params = Arrays.copyOfRange(lowercaseParams, 0, lowercaseParams.length);
        } else {
            params = new String[]{};
        }

        command.execute(client, params);
        log.info(I18nUtil.getLogMessage("CommandsExecutor.handleInternal.info1"), client.getPlayer().getName(), command.getClass().getSimpleName());
    }

    private void addCommandInfo(String name, Class<? extends Command> commandClass) {
        try {
            levelCommandsCursor.getRight().add(commandClass.getDeclaredConstructor().newInstance().getDescription());
            levelCommandsCursor.getLeft().add(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCommand(String[] syntaxs, Class<? extends Command> commandClass) {
        for (String syntax : syntaxs) {
            addCommand(syntax, 0, commandClass);
        }
    }

    private void addCommand(String syntax, Class<? extends Command> commandClass) {
        //for (String syntax : syntaxs){
        addCommand(syntax, 0, commandClass);
        //}
    }

    private void addCommand(String[] surtaxes, int rank, Class<? extends Command> commandClass) {
        for (String syntax : surtaxes) {
            addCommand(syntax, rank, commandClass);
        }
    }

    private void addCommand(String syntax, int rank, Class<? extends Command> commandClass) {
        if (registeredCommands.containsKey(syntax.toLowerCase())) {
            log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn1"), syntax);
            return;
        }

        String commandName = syntax.toLowerCase();
        addCommandInfo(commandName, commandClass);

        try {
            Command commandInstance = commandClass.getDeclaredConstructor().newInstance();     // thanks Halcyon for noticing commands getting reinstanced every call
            commandInstance.setRank(rank);

            registeredCommands.put(commandName, commandInstance);
        } catch (Exception e) {
            log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn2"), e);
        }
    }

    private void registerLv0Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand(new String[]{"help", "commands"}, HelpCommand.class);
        addCommand("droplimit", DropLimitCommand.class);
        addCommand("time", TimeCommand.class);
        addCommand("credits", StaffCommand.class);
        addCommand("uptime", UptimeCommand.class);
        addCommand("gacha", GachaCommand.class);
        addCommand("dispose", DisposeCommand.class);
        addCommand("changel", ChangeLanguageCommand.class);
        addCommand("equiplv", EquipLvCommand.class);
        addCommand("showrates", ShowRatesCommand.class);
        addCommand("rates", RatesCommand.class);
        addCommand("online", OnlineCommand.class);
        addCommand("gm", GmCommand.class);
        addCommand("reportbug", ReportBugCommand.class);
        addCommand("points", ReadPointsCommand.class);
        addCommand("joinevent", JoinEventCommand.class);
        addCommand("leaveevent", LeaveEventCommand.class);
        addCommand("ranks", RanksCommand.class);
        addCommand("str", StatStrCommand.class);
        addCommand("dex", StatDexCommand.class);
        addCommand("int", StatIntCommand.class);
        addCommand("luk", StatLukCommand.class);
        addCommand("enableauth", EnableAuthCommand.class);
        addCommand("toggleexp", ToggleExpCommand.class);
        addCommand("mylawn", MapOwnerClaimCommand.class);
        addCommand("bosshp", BossHpCommand.class);
        addCommand("mobhp", MobHpCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }


    private void registerLv1Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("whatdropsfrom", 1, WhatDropsFromCommand.class);
        addCommand("whodrops", 1, WhoDropsCommand.class);
        addCommand("buffme", 1, BuffMeCommand.class);
        addCommand("goto", 1, GotoCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }


    private void registerLv2Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("recharge", 2, RechargeCommand.class);
        addCommand("whereami", 2, WhereaMiCommand.class);
        addCommand("hide", 2, HideCommand.class);
        addCommand("unhide", 2, UnHideCommand.class);
        addCommand("sp", 2, SpCommand.class);
        addCommand("ap", 2, ApCommand.class);
        addCommand("empowerme", 2, EmpowerMeCommand.class);
        addCommand("buffmap", 2, BuffMapCommand.class);
        addCommand("buff", 2, BuffCommand.class);
        addCommand("bomb", 2, BombCommand.class);
        addCommand("dc", 2, DcCommand.class);
        addCommand("cleardrops", 2, ClearDropsCommand.class);
        addCommand("clearslot", 2, ClearSlotCommand.class);
        addCommand("clearsavelocs", 2, ClearSavedLocationsCommand.class);
        addCommand("warp", 2, WarpCommand.class);
        addCommand(new String[]{"warphere", "summon"}, 2, SummonCommand.class);
        addCommand(new String[]{"warpto", "reach", "follow"}, 2, ReachCommand.class);
        addCommand("gmshop", 2, GmShopCommand.class);
        addCommand("heal", 2, HealCommand.class);
        addCommand("item", 2, ItemCommand.class);
        addCommand("drop", 2, ItemDropCommand.class);
        addCommand("level", 2, LevelCommand.class);
        addCommand("levelpro", 2, LevelProCommand.class);
        addCommand("setslot", 2, SetSlotCommand.class);
        addCommand("setstat", 2, SetStatCommand.class);
        addCommand("maxstat", 2, MaxStatCommand.class);
        addCommand("maxskill", 2, MaxSkillCommand.class);
        addCommand("resetskill", 2, ResetSkillCommand.class);
        addCommand("search", 2, SearchCommand.class);
        addCommand("jail", 2, JailCommand.class);
        addCommand("unjail", 2, UnJailCommand.class);
        addCommand("job", 2, JobCommand.class);
        addCommand("unbug", 2, UnBugCommand.class);
        addCommand("id", 2, IdCommand.class);
        addCommand("gachalist", 2, GachaListCommand.class);
        addCommand("loot", 2, LootCommand.class);
        addCommand("mobskill", 2, MobSkillCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv3Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("debuff", 3, DebuffCommand.class);
        addCommand("fly", 3, FlyCommand.class);
        addCommand("spawn", 3, SpawnCommand.class);
        addCommand("mutemap", 3, MuteMapCommand.class);
        addCommand("checkdmg", 3, CheckDmgCommand.class);
        addCommand("inmap", 3, InMapCommand.class);
        addCommand("reloadevents", 3, ReloadEventsCommand.class);
        addCommand("reloaddrops", 3, ReloadDropsCommand.class);
        addCommand("reloadportals", 3, ReloadPortalsCommand.class);
        addCommand("reloadmap", 3, ReloadMapCommand.class);
        addCommand("reloadshops", 3, ReloadShopsCommand.class);
        addCommand("hpmp", 3, HpMpCommand.class);
        addCommand("maxhpmp", 3, MaxHpMpCommand.class);
        addCommand("music", 3, MusicCommand.class);
        addCommand("monitor", 3, MonitorCommand.class);
        addCommand("monitors", 3, MonitorsCommand.class);
        addCommand("ignore", 3, IgnoreCommand.class);
        addCommand("ignored", 3, IgnoredCommand.class);
        addCommand("pos", 3, PosCommand.class);
        addCommand("togglecoupon", 3, ToggleCouponCommand.class);
        addCommand("togglewhitechat", 3, ChatCommand.class);
        addCommand("fame", 3, FameCommand.class);
        addCommand("givenx", 3, GiveNxCommand.class);
        addCommand("givevp", 3, GiveVpCommand.class);
        addCommand("givems", 3, GiveMesosCommand.class);
        addCommand("giverp", 3, GiveRpCommand.class);
        addCommand("expeds", 3, ExpedsCommand.class);
        addCommand("kill", 3, KillCommand.class);
        addCommand("seed", 3, SeedCommand.class);
        addCommand("maxenergy", 3, MaxEnergyCommand.class);
        addCommand("killall", 3, KillAllCommand.class);
        addCommand("notice", 3, NoticeCommand.class);
        addCommand("rip", 3, RipCommand.class);
        addCommand("openportal", 3, OpenPortalCommand.class);
        addCommand("closeportal", 3, ClosePortalCommand.class);
        addCommand("pe", 3, PeCommand.class);
        addCommand("startevent", 3, StartEventCommand.class);
        addCommand("endevent", 3, EndEventCommand.class);
        addCommand("startmapevent", 3, StartMapEventCommand.class);
        addCommand("stopmapevent", 3, StopMapEventCommand.class);
        addCommand("online2", 3, OnlineTwoCommand.class);
        addCommand("ban", 3, BanCommand.class);
        addCommand("unban", 3, UnBanCommand.class);
        addCommand("healmap", 3, HealMapCommand.class);
        addCommand("healperson", 3, HealPersonCommand.class);
        addCommand("hurt", 3, HurtCommand.class);
        addCommand("killmap", 3, KillMapCommand.class);
        addCommand("night", 3, NightCommand.class);
        addCommand("npc", 3, NpcCommand.class);
        addCommand("face", 3, FaceCommand.class);
        addCommand("hair", 3, HairCommand.class);
        addCommand("startquest", 3, QuestStartCommand.class);
        addCommand("completequest", 3, QuestCompleteCommand.class);
        addCommand("resetquest", 3, QuestResetCommand.class);
        addCommand("timer", 3, TimerCommand.class);
        addCommand("timermap", 3, TimerMapCommand.class);
        addCommand("timerall", 3, TimerAllCommand.class);
        addCommand("warpmap", 3, WarpMapCommand.class);
        addCommand("warparea", 3, WarpAreaCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv4Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("servermessage", 4, ServerMessageCommand.class);
        addCommand("proitem", 4, ProItemCommand.class);
        addCommand("seteqstat", 4, SetEqStatCommand.class);
        addCommand("exprate", 4, ExpRateCommand.class);
        addCommand("mesorate", 4, MesoRateCommand.class);
        addCommand("droprate", 4, DropRateCommand.class);
        addCommand("bossdroprate", 4, BossDropRateCommand.class);
        addCommand("questrate", 4, QuestRateCommand.class);
        addCommand("travelrate", 4, TravelRateCommand.class);
        addCommand("fishrate", 4, FishingRateCommand.class);
        addCommand("itemvac", 4, ItemVacCommand.class);
        addCommand("forcevac", 4, ForceVacCommand.class);
        addCommand("zakum", 4, ZakumCommand.class);
        addCommand("horntail", 4, HorntailCommand.class);
        addCommand("pinkbean", 4, PinkbeanCommand.class);
        addCommand("pap", 4, PapCommand.class);
        addCommand("pianus", 4, PianusCommand.class);
        addCommand("cake", 4, CakeCommand.class);
        addCommand("playernpc", 4, PlayerNpcCommand.class);
        addCommand("playernpcremove", 4, PlayerNpcRemoveCommand.class);
        addCommand("pnpc", 4, PnpcCommand.class);
        addCommand("pnpcremove", 4, PnpcRemoveCommand.class);
        addCommand("pmob", 4, PmobCommand.class);
        addCommand("pmobremove", 4, PmobRemoveCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv5Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("debug", 5, DebugCommand.class);
        addCommand("set", 5, SetCommand.class);
        addCommand("showpackets", 5, ShowPacketsCommand.class);
        addCommand("showmovelife", 5, ShowMoveLifeCommand.class);
        addCommand("showsessions", 5, ShowSessionsCommand.class);
        addCommand("iplist", 5, IpListCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv6Commands() {
        levelCommandsCursor = new Pair<>(new ArrayList<String>(), new ArrayList<String>());

        addCommand("setgmlevel", 6, SetGmLevelCommand.class);
        addCommand("warpworld", 6, WarpWorldCommand.class);
        addCommand("saveall", 6, SaveAllCommand.class);
        addCommand("dcall", 6, DCAllCommand.class);
        addCommand("mapplayers", 6, MapPlayersCommand.class);
        addCommand("getacc", 6, GetAccCommand.class);
        addCommand("shutdown", 6, ShutdownCommand.class);
        addCommand("clearquestcache", 6, ClearQuestCacheCommand.class);
        addCommand("clearquest", 6, ClearQuestCommand.class);
        addCommand("supplyratecoupon", 6, SupplyRateCouponCommand.class);
        addCommand("spawnallpnpcs", 6, SpawnAllPNpcsCommand.class);
        addCommand("eraseallpnpcs", 6, EraseAllPNpcsCommand.class);
        addCommand("addchannel", 6, ServerAddChannelCommand.class);
        addCommand("addworld", 6, ServerAddWorldCommand.class);
        addCommand("removechannel", 6, ServerRemoveChannelCommand.class);
        addCommand("removeworld", 6, ServerRemoveWorldCommand.class);
        addCommand("devtest", 6, DevtestCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

}
