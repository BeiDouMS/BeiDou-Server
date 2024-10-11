package org.gms.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.command.Command;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.dao.mapper.CommandInfoMapper;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CommandService {

    private final CommandInfoMapper commandInfoMapper;

    public void loadCommands(final HashMap<String, Command> registeredCommands,
                             final List<Pair<List<String>, List<String>>> commandsNameDesc) {
        registeredCommands.clear();
        commandsNameDesc.clear();

        List<CommandInfoDO> commandInfoList = commandInfoMapper.selectAll();
        if (commandInfoList == null || commandInfoList.isEmpty()) {
            log.warn("commandInfo list  is null");
            return;
        }
        // 根据level对指令分组
        Map<Integer, List<CommandInfoDO>> levelMap = commandInfoList.stream()
                .collect(Collectors.groupingBy(CommandInfoDO::getLevel));

        registerCommands(registeredCommands, commandsNameDesc, 0, levelMap.get(0));
        registerCommands(registeredCommands, commandsNameDesc, 1, levelMap.get(1));
        registerCommands(registeredCommands, commandsNameDesc, 2, levelMap.get(2));
        registerCommands(registeredCommands, commandsNameDesc, 3, levelMap.get(3));
        registerCommands(registeredCommands, commandsNameDesc, 4, levelMap.get(4));
        registerCommands(registeredCommands, commandsNameDesc, 5, levelMap.get(5));
        registerCommands(registeredCommands, commandsNameDesc, 6, levelMap.get(6));

        log.info("加载指令结束，指令数量：{}", registeredCommands.size());
    }

    private void registerCommands(final HashMap<String, Command> registeredCommands,
                                  final List<Pair<List<String>, List<String>>> commandsNameDesc,
                                  int level,
                                  List<CommandInfoDO> commandInfoList) {
        if (commandInfoList == null || commandInfoList.isEmpty()) {
            log.warn("gm level={}, command list is empty!", level);
            return;
        }

        Pair<List<String>, List<String>> levelCommandsCursor = new Pair<>(new ArrayList<>(), new ArrayList<>());
        for (CommandInfoDO item : commandInfoList) {
            Class<? extends Command> aClass = this.getCommandClass(item);
            if (aClass == null) {
                log.warn("command is null, command={}, clazz={}", item.getSyntax(), item.getClazz());
                continue;
            }

            String commandName = item.getSyntax().toLowerCase();

            //log.info("add command={} class={}", item.getSyntax(), aClass.getName());
            if (registeredCommands.containsKey(commandName)) {
                log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn1"), item.getSyntax());
                continue;
            }

            try {
                levelCommandsCursor.getRight().add(aClass.getDeclaredConstructor().newInstance().getDescription());
                levelCommandsCursor.getLeft().add(commandName);

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
                    + "." + commandInfoDO.getClazz());
            return (Class<? extends Command>) aClass;
        } catch (ClassNotFoundException e) {
            log.error("getCommandClass error: {}", e.getMessage());
            return null;
        }
    }

}
