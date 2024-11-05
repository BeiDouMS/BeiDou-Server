package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.config.GameConfig;
import org.gms.dao.entity.GameConfigDO;
import org.gms.dao.mapper.GameConfigMapper;
import org.gms.model.dto.ConfigTypeDTO;
import org.gms.model.dto.GameConfigReqDTO;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.distinct;
import static org.gms.dao.entity.table.GameConfigDOTableDef.GAME_CONFIG_D_O;

@Service
@AllArgsConstructor
public class ConfigService {
    private final GameConfigMapper gameConfigMapper;

    public List<GameConfigDO> loadGameConfigs() {
        return gameConfigMapper.selectAll();
    }

    public ConfigTypeDTO getConfigTypeList() {
        List<GameConfigDO> typeDOList = gameConfigMapper.selectListByQuery(QueryWrapper.create().select(distinct(GAME_CONFIG_D_O.CONFIG_TYPE)));
        List<GameConfigDO> subTypeDOList = gameConfigMapper.selectListByQuery(QueryWrapper.create().select(distinct(GAME_CONFIG_D_O.CONFIG_SUB_TYPE)));
        return ConfigTypeDTO.builder()
                .types(typeDOList.stream().map(GameConfigDO::getConfigType).toList())
                .subTypes(subTypeDOList.stream().map(GameConfigDO::getConfigSubType).toList())
                .build();
    }

    public Page<GameConfigDO> getConfigList(GameConfigReqDTO condition) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (!RequireUtil.isEmpty(condition.getType()))
            queryWrapper.and(GAME_CONFIG_D_O.CONFIG_TYPE.eq(condition.getType()));
        if (!RequireUtil.isEmpty(condition.getSubType()))
            queryWrapper.and(GAME_CONFIG_D_O.CONFIG_SUB_TYPE.eq(condition.getSubType()));
        if (!RequireUtil.isEmpty(condition.getFilter())) {
            queryWrapper.and(GAME_CONFIG_D_O.CONFIG_CODE.like(condition.getFilter()).or(GAME_CONFIG_D_O.CONFIG_DESC.like(condition.getFilter())));
        }

        return gameConfigMapper.paginate(condition.getPageNo(), condition.getPageSize(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addConfig(GameConfigDO condition) {
        RequireUtil.requireNotEmpty(condition.getConfigType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "configType"));
        RequireUtil.requireNotEmpty(condition.getConfigSubType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "configSubType"));
        RequireUtil.requireNotEmpty(condition.getConfigCode(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "configCode"));
        RequireUtil.requireNotEmpty(condition.getConfigValue(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "configValue"));
        List<GameConfigDO> gameConfigDOList = gameConfigMapper.selectListByQuery(QueryWrapper.create()
                .where(GAME_CONFIG_D_O.CONFIG_TYPE.eq(condition.getConfigType()))
                .where(GAME_CONFIG_D_O.CONFIG_SUB_TYPE.eq(condition.getConfigSubType()))
                .and(GAME_CONFIG_D_O.CONFIG_CODE.eq(condition.getConfigCode())));
        RequireUtil.requireTrue(gameConfigDOList.isEmpty(), I18nUtil.getExceptionMessage("ConfigService.addConfig.exception1"));
        condition.setId(null);
        gameConfigMapper.insertSelective(condition);
        GameConfig.add(condition);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(GameConfigDO condition) {
        RequireUtil.requireNotNull(condition.getId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));
        RequireUtil.requireNotEmpty(condition.getConfigValue(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "configValue"));
        gameConfigMapper.update(GameConfigDO.builder()
                .id(condition.getId())
                .configValue(condition.getConfigValue())
                .configDesc(condition.getConfigDesc())
                .build());
        GameConfigDO gameConfigDO = gameConfigMapper.selectOneById(condition.getId());
        GameConfig.update(gameConfigDO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        RequireUtil.requireNotNull(id, I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL", "id"));
        GameConfigDO gameConfigDO = gameConfigMapper.selectOneById(id);
        gameConfigMapper.deleteById(id);
        GameConfig.remove(gameConfigDO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigList(List<Long> ids) {
        RequireUtil.requireNotEmpty(ids, I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "ids"));
        List<GameConfigDO> gameConfigDOS = gameConfigMapper.selectListByIds(ids);
        gameConfigMapper.deleteBatchByIds(ids);
        gameConfigDOS.forEach(GameConfig::remove);
    }
}
