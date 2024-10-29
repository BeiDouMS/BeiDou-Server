package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.command.Command;
import org.gms.client.command.CommandsExecutor;
import org.gms.dao.entity.CommandInfoDO;
import org.gms.dao.mapper.CommandInfoMapper;
import org.gms.exception.BizException;
import org.gms.model.dto.CommandReqDTO;
import org.gms.model.dto.DropSearchRtnDTO;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        for (int i = 0; i <= 6; i++) {
            registerCommands(registeredCommands, commandsNameDesc, i, levelMap.get(i));
        }
        log.info("加载指令结束，指令数量：{}", registeredCommands.size());
    }


    private void updateRegisteredCommands(CommandInfoDO commandInfoDO) {
        CommandsExecutor commandsExecutor = CommandsExecutor.getInstance();
        HashMap<String, Command> registeredCommands = commandsExecutor.getRegisteredCommands();
        List<Pair<List<String>, List<String>>> commandsNameDesc = commandsExecutor.getCommandsNameDesc();
        String syntax = commandInfoDO.getSyntax().toLowerCase();
        Command command = registeredCommands.get(syntax);
        // 如果原先未注册
        if (command == null) {
            // 如果更新的状态是开启，则添加注册。如果新状态是关闭，则不必理会，更新db即可
            if (commandInfoDO.isEnabled()) {
                command = getCommandInstance(commandInfoDO);
                RequireUtil.requireNotNull(command, I18nUtil.getExceptionMessage("UNKNOWN_PARAMETER_VALUE", "clazz", commandInfoDO.getClazz()));
                registeredCommands.put(syntax, command);
                // 按照新等级获取实例
                Pair<List<String>, List<String>> nameDescPair = commandsNameDesc.get(commandInfoDO.getLevel());
                // 添加到最后
                nameDescPair.getLeft().add(syntax);
                nameDescPair.getRight().add(command.getDescription());
            }
            return;
        }
        // 原先已注册，这里拿的是老的rank去获取老的nameDesc
        Pair<List<String>, List<String>> oldPair = commandsNameDesc.get(command.getRank());
        int index = oldPair.getLeft().indexOf(syntax);
        // 获取index，移除name和desc
        if (index > -1) {
            oldPair.getLeft().remove(index);
            oldPair.getRight().remove(index);
        }

        // 如果新状态是开启，那么有可能是更新了等级。如果新状态是关闭，则移除之前的注册
        if (commandInfoDO.isEnabled()) {
            // 老的nameDesc已经移除，这里直接把新的等级加进nameDesc
            Pair<List<String>, List<String>> newPair = commandsNameDesc.get(commandInfoDO.getId());
            newPair.getLeft().add(syntax);
            newPair.getRight().add(command.getDescription());

            // 更新reg的Rank
            command.setRank(commandInfoDO.getLevel());
        } else {
            // 老的nameDesc已经移除，这里直接移除reg
            registeredCommands.remove(syntax);
        }

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
            // 未开启的不能加载
            if (!item.isEnabled()) {
                continue;
            }
            Command command = getCommandInstance(item);
            if (command == null) {
                log.warn("command is null, command={}, clazz={}", item.getSyntax(), item.getClazz());
                continue;
            }

            String commandName = item.getSyntax().toLowerCase();
            if (registeredCommands.containsKey(commandName)) {
                log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn1"), item.getSyntax());
                continue;
            }

            try {
                levelCommandsCursor.getRight().add(command.getDescription());
                levelCommandsCursor.getLeft().add(commandName);
                registeredCommands.put(commandName, command);
            } catch (Exception e) {
                log.warn(I18nUtil.getLogMessage("CommandsExecutor.addCommand.warn2"), e);
            }
        }

        commandsNameDesc.add(levelCommandsCursor);
    }

    private Command getCommandInstance(CommandInfoDO commandInfoDO) {
        try {
            Class<?> aClass = Class.forName("org.gms.client.command.commands.gm" + commandInfoDO.getDefaultLevel()
                    + "." + commandInfoDO.getClazz());
            Command command = (Command) aClass.getDeclaredConstructor().newInstance();
            command.setRank(commandInfoDO.getLevel());
            return command;
        } catch (Exception e) {
            log.error("getCommandInstance error: {}", e.getMessage());
            return null;
        }
    }

    public Page<CommandReqDTO> getCommandListFromDB(CommandReqDTO request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (request.getLevel() != null) queryWrapper.in("level", request.getLevelList());
        if (request.getDefaultLevel() != null) queryWrapper.in("default_level", request.getDefaultLevelList());
        if (request.getSyntax() != null) queryWrapper.like("syntax", request.getSyntax());

        if (request.getEnabled() != null) queryWrapper.eq("enabled", request.getEnabled());
        Page<CommandInfoDO> commandInfoDOPage = commandInfoMapper.paginateWithRelations(request.getPage(), request.getPageSize(), queryWrapper);
        return new Page<>(
                commandInfoDOPage.getRecords().stream()
                        .map(
                                record -> CommandReqDTO.builder()
                                        .id(record.getId())
                                        .level(record.getLevel())
                                        .syntax(record.getSyntax())
                                        .defaultLevel(record.getDefaultLevel())
                                        .clazz(record.getClazz())
                                        .enabled(record.isEnabled())
                                        .description(getDescriptionByCommandInfoDO(record))
                                        .build())
                        .toList(),
                commandInfoDOPage.getPageNumber(),
                commandInfoDOPage.getPageSize(),
                commandInfoDOPage.getTotalRow()
        );


    }

    public String getDescriptionByCommandInfoDO(CommandInfoDO CommandDO) {
        return getCommandInstance(CommandDO).getDescription();
    }

    @Transactional
    public CommandInfoDO updateCommand(CommandReqDTO request) {

        RequireUtil.requireNotNull(request.getEnabled(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "enable"));
        RequireUtil.requireNotNull(request.getId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));


        CommandInfoDO updateCommandInfoDO = new CommandInfoDO();
        updateCommandInfoDO.setId(request.getId());
        /**
         * 只能改开关和等级，其他的不能改
         * Syntax改了，指令和提示会冲突，比如提示：输入：!level <等级>就是错的了
         * 因为level已经被改成其他的了
         * DefaultLevel和Clazz也不能改
         */
        updateCommandInfoDO.setLevel(request.getLevel() != null ? request.getLevel() : updateCommandInfoDO.getLevel());

        if (!request.getEnabled()) {
            updateCommandInfoDO.setEnabled(false);
        } else {
            updateCommandInfoDO.setEnabled(true);
        }
        //commandInfoDO.setDescription(request.getDescription());//i18n工具,添加命令功能作用描述
        commandInfoMapper.update(updateCommandInfoDO);
        updateRegisteredCommands(updateCommandInfoDO);
        return updateCommandInfoDO;
    }


}
