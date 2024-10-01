package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
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

@Slf4j
@Service
@AllArgsConstructor
public class CommandService {

    private final CommandInfoMapper commandInfoMapper;

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

        log.info("加载指令结束，指令数量：{}", registeredCommands.size());
    }

    private void registerCommands(final HashMap<String, Command> registeredCommands,
                                  final List<Pair<List<String>, List<String>>> commandsNameDesc,
                                  int level) {
        Pair<List<String>, List<String>> levelCommandsCursor = new Pair<>(new ArrayList<>(), new ArrayList<>());

        List<CommandInfoDO> commandInfoList = queryCommandsByLevel(level);
        for (CommandInfoDO item : commandInfoList) {
            Class<? extends Command> aClass = this.getCommandClass(item);
            if (aClass == null) {
                log.warn("command is null, command={}, commandClassName={}", item.getSyntax(), item.getCommandClassName());
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
                    + "." + commandInfoDO.getCommandClassName());
            return (Class<? extends Command>) aClass;
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private List<CommandInfoDO> queryCommandsByLevel(int level) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(CommandInfoDO::getLevel).eq(level);
        List<CommandInfoDO> results = commandInfoMapper.selectListByQuery(queryWrapper);
        return results;
    }

}
