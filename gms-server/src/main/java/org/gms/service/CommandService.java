package org.gms.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.command.Command;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CommandService {

    public void loadCommands(final HashMap<String, Command> registeredCommands,
                             final List<Pair<List<String>, List<String>>> commandsNameDesc) {
        registeredCommands.clear();
        commandsNameDesc.clear();
        registerCommands(registeredCommands, commandsNameDesc, 0);
        registerCommands(registeredCommands, commandsNameDesc, 1);
        registerCommands(registeredCommands, commandsNameDesc, 2);
        registerCommands(registeredCommands, commandsNameDesc, 3);
        registerCommands(registeredCommands, commandsNameDesc, 4);
        registerCommands(registeredCommands, commandsNameDesc, 5);
        registerCommands(registeredCommands, commandsNameDesc, 6);

        log.info("loadCommands success registeredCommands size={}", registeredCommands.size());
    }

    private void registerCommands(final HashMap<String, Command> registeredCommands,
                                  final List<Pair<List<String>, List<String>>> commandsNameDesc,
                                  int level) {
        Pair<List<String>, List<String>> levelCommandsCursor = new Pair<>(new ArrayList<>(), new ArrayList<>());

        List<CommandInfoDO> commandInfoList = queryCommandsByLevel(level);
        for (CommandInfoDO item : commandInfoList) {
            Class<? extends Command> aClass = this.getCommandClass(item);
            if (aClass == null) {
                log.warn(" command is null, command={}, commandClassName={}", item.getSyntax(), item.getCommandClassName());
                continue;
            }

            //log.info("add command={} class={}", item.getSyntax(), aClass.getName());
            if (registeredCommands.containsKey(item.getSyntax().toLowerCase())) {
                log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn1"), item.getSyntax());
                continue;
            }

            String commandName = item.getSyntax().toLowerCase();
            try {
                levelCommandsCursor.getRight().add(aClass.getDeclaredConstructor().newInstance().getDescription());
                levelCommandsCursor.getLeft().add(commandName);
            } catch (Exception e) {
                log.warn("", e);
            }

            try {
                Command commandInstance = aClass.getDeclaredConstructor().newInstance();
                commandInstance.setRank(level);

                registeredCommands.put(commandName, commandInstance);
            } catch (Exception e) {
                log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn2"), e);
            }
        }

        commandsNameDesc.add(levelCommandsCursor);
    }

    private Class<? extends Command> getCommandClass(CommandInfoDO commandInfoDO) {
        try {
            Class<?> aClass = Class.forName("org.gms.client.command.commands.gm" + commandInfoDO.getDefaultLevel()
                    + "." + commandInfoDO.getCommandClassName());
            return (Class<? extends Command>) aClass;
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private List<CommandInfoDO> queryCommandsByLevel(int level) {
        List<CommandInfoDO> results = new ArrayList<>();

        // TODO 查询数据库
        if (level == 0) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("help");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("HelpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("commands");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("HelpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("droplimit");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("DropLimitCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("time");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("TimeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("credits");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("StaffCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("uptime");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("UptimeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("gacha");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("GachaCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("dispose");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("DisposeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("changel");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("ChangeLanguageCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("equiplv");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("EquipLvCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("showrates");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("ShowRatesCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("rates");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("RatesCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("online");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("OnlineCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("gm");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("GmCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reportbug");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("ReportBugCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("points");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("ReadPointsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("joinevent");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("JoinEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("leaveevent");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("LeaveEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("ranks");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("RanksCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("str");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("StatStrCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("dex");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("StatDexCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("int");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("StatIntCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("luk");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("StatLukCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("enableauth");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("EnableAuthCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("toggleexp");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("ToggleExpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mylawn");
            commandInfoDO.setLevel(0);
            commandInfoDO.setDefaultLevel(0);
            commandInfoDO.setCommandClassName("MapOwnerClaimCommand");
            results.add(commandInfoDO);

        } else if (level == 1) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("whatdropsfrom");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("WhatDropsFromCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("bosshp");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("BossHpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mobhp");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("MobHpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("whodrops");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("WhoDropsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("buffme");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("BuffMeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("goto");
            commandInfoDO.setLevel(1);
            commandInfoDO.setDefaultLevel(1);
            commandInfoDO.setCommandClassName("GotoCommand");
            results.add(commandInfoDO);

        } else if (level == 2) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("recharge");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("RechargeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("whereami");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("WhereaMiCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("hide");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("HideCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("unhide");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("UnHideCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("sp");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("ap");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ApCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("empowerme");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("EmpowerMeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("buffmap");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("BuffMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("buff");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("BuffCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("bomb");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("BombCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("dc");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("DcCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("cleardrops");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ClearDropsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("clearslot");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ClearSlotCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("clearsavelocs");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ClearSavedLocationsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warp");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("WarpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warphere");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SummonCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("summon");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SummonCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warpto");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ReachCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reach");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ReachCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("follow");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ReachCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("gmshop");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("GmShopCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("heal");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("HealCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("item");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ItemCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("drop");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ItemDropCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("level");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("LevelCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("levelpro");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("LevelProCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("setslot");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SetSlotCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("setstat");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SetStatCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("maxstat");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("MaxStatCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("maxskill");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("MaxSkillCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("resetskill");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("ResetSkillCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("search");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("SearchCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("jail");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("JailCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("unjail");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("UnJailCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("job");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("JobCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("unbug");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("UnBugCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("id");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("IdCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("gachalist");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("GachaListCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("loot");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("LootCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mobskill");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("MobSkillCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warpmap");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("WarpMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warparea");
            commandInfoDO.setLevel(2);
            commandInfoDO.setDefaultLevel(2);
            commandInfoDO.setCommandClassName("WarpAreaCommand");
            results.add(commandInfoDO);

        } else if (level == 3) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("debuff");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("DebuffCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("fly");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("FlyCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("spawn");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("SpawnCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mutemap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MuteMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("checkdmg");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("CheckDmgCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("inmap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("InMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reloadevents");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ReloadEventsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reloaddrops");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ReloadDropsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reloadportals");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ReloadPortalsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reloadmap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ReloadMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("reloadshops");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ReloadShopsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("hpmp");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("HpMpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("maxhpmp");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MaxHpMpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("music");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MusicCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("monitor");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MonitorCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("monitors");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MonitorsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("ignore");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("IgnoreCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("ignored");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("IgnoredCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pos");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("PosCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("togglecoupon");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ToggleCouponCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("togglewhitechat");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ChatCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("fame");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("FameCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("givenx");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("GiveNxCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("givevp");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("GiveVpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("givems");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("GiveMesosCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("giverp");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("GiveRpCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("expeds");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ExpedsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("kill");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("KillCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("seed");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("SeedCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("maxenergy");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("MaxEnergyCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("killall");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("KillAllCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("notice");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("NoticeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("rip");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("RipCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("openportal");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("OpenPortalCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("closeportal");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("ClosePortalCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pe");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("PeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("startevent");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("StartEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("endevent");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("EndEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("startmapevent");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("StartMapEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("stopmapevent");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("StopMapEventCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("online2");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("OnlineTwoCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("ban");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("BanCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("unban");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("UnBanCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("healmap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("HealMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("healperson");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("HealPersonCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("hurt");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("HurtCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("killmap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("KillMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("night");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("NightCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("npc");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("NpcCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("face");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("FaceCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("hair");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("HairCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("startquest");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("QuestStartCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("completequest");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("QuestCompleteCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("resetquest");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("QuestResetCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("timer");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("TimerCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("timermap");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("TimerMapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("timerall");
            commandInfoDO.setLevel(3);
            commandInfoDO.setDefaultLevel(3);
            commandInfoDO.setCommandClassName("TimerAllCommand");
            results.add(commandInfoDO);

        } else if (level == 4) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("servermessage");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ServerMessageCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("proitem");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ProItemCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("seteqstat");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("SetEqStatCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("exprate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ExpRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mesorate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("MesoRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("droprate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("DropRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("bossdroprate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("BossDropRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("questrate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("QuestRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("travelrate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("TravelRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("fishrate");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("FishingRateCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("itemvac");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ItemVacCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("forcevac");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ForceVacCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("zakum");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("ZakumCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("horntail");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("HorntailCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pinkbean");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PinkbeanCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pap");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PapCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pianus");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PianusCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("cake");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("CakeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("playernpc");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PlayerNpcCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("playernpcremove");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PlayerNpcRemoveCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pnpc");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PnpcCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pnpcremove");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PnpcRemoveCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pmob");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PmobCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("pmobremove");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("PmobRemoveCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warptolife");
            commandInfoDO.setLevel(4);
            commandInfoDO.setDefaultLevel(4);
            commandInfoDO.setCommandClassName("WarpToLifeCommand");
            results.add(commandInfoDO);

        } else if (level == 5) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("debug");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("DebugCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("set");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("SetCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("showpackets");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("ShowPacketsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("showmovelife");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("ShowMoveLifeCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("showsessions");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("ShowSessionsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("iplist");
            commandInfoDO.setLevel(5);
            commandInfoDO.setDefaultLevel(5);
            commandInfoDO.setCommandClassName("IpListCommand");
            results.add(commandInfoDO);

        } else if (level == 6) {
            CommandInfoDO commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("setgmlevel");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("SetGmLevelCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("warpworld");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("WarpWorldCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("saveall");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("SaveAllCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("dcall");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("DCAllCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("mapplayers");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("MapPlayersCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("getacc");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("GetAccCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("shutdown");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ShutdownCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("clearquestcache");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ClearQuestCacheCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("clearquest");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ClearQuestCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("supplyratecoupon");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("SupplyRateCouponCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("spawnallpnpcs");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("SpawnAllPNpcsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("eraseallpnpcs");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("EraseAllPNpcsCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("addchannel");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ServerAddChannelCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("addworld");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ServerAddWorldCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("removechannel");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ServerRemoveChannelCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("removeworld");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("ServerRemoveWorldCommand");
            results.add(commandInfoDO);

            commandInfoDO = new CommandInfoDO();
            commandInfoDO.setSyntax("devtest");
            commandInfoDO.setLevel(6);
            commandInfoDO.setDefaultLevel(6);
            commandInfoDO.setCommandClassName("DevtestCommand");
            results.add(commandInfoDO);
        }
        return results;
    }

}
