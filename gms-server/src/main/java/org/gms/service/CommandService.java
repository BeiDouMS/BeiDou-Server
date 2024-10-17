package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.command.Command;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.dao.mapper.CommandInfoMapper;
import org.gms.model.dto.CommandReqDTO;
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

    public Page<CommandInfoDO> getCommandListFromDB(CommandReqDTO request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (request.getLevel() != null) queryWrapper.in("level", request.getLevelList());
        if (request.getDefaultLevel() != null) queryWrapper.in("default_level", request.getDefaultLevelList());
        if (request.getSyntax() != null) queryWrapper.like("syntax", request.getSyntax());
        //if (request.getDescription() != null) queryWrapper.like("description", request.getDescription());
        if (request.getEnabled() != null) queryWrapper.eq("enabled", request.getEnabled());
        return commandInfoMapper.paginateWithRelations(request.getPage(), request.getPageSize(), queryWrapper);

    }
    public CommandInfoDO updateCommand(CommandReqDTO request) {

        CommandInfoDO updateCommandInfoDO = commandInfoMapper.selectOneById(request.getId());
        /**
         * 只能改开关和等级，其他的不能改
         * Syntax改了，指令和提示会冲突，比如提示：输入：!level <等级>就是错的了
         * 因为level已经被改成其他的了
         * DefaultLevel和Clazz也不能改
         */
        updateCommandInfoDO.setLevel(request.getLevel() != null ? request.getLevel() : updateCommandInfoDO.getLevel());
        updateCommandInfoDO.setEnabled(request.getEnabled() != null ? request.getEnabled() : updateCommandInfoDO.getEnabled());
        //commandInfoDO.setDescription(request.getDescription());//i18n工具,添加命令功能作用描述
        commandInfoMapper.update(updateCommandInfoDO);
        return updateCommandInfoDO;
    }
    public CommandInfoDO insertCommandInfo(CommandReqDTO request) {

        CommandInfoDO insertCommandInfoDO = new CommandInfoDO();
        insertCommandInfoDO.setLevel(request.getLevel() != null ? request.getLevel() : insertCommandInfoDO.getLevel());
        insertCommandInfoDO.setSyntax(request.getSyntax() != null ? request.getSyntax() : insertCommandInfoDO.getSyntax());
        insertCommandInfoDO.setDefaultLevel(request.getDefaultLevel() != null ? request.getDefaultLevel() : insertCommandInfoDO.getDefaultLevel());
        insertCommandInfoDO.setEnabled(request.getEnabled() != null ? request.getEnabled() : insertCommandInfoDO.getEnabled());
        insertCommandInfoDO.setClazz(request.getClazz() != null ? request.getClazz() : insertCommandInfoDO.getClazz());
        //insertCommandInfoDO.setDescription(request.getDescription());//i18n工具,添加命令功能作用描述
        commandInfoMapper.insert(insertCommandInfoDO);
        return insertCommandInfoDO;
    }


}
