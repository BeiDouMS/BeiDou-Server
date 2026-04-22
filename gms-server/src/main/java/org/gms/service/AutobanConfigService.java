package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.autoban.AutobanFactory;
import org.gms.dao.entity.AutobanConfigDO;
import org.gms.dao.mapper.AutobanConfigMapper;
import org.gms.model.dto.AutobanConfigDTO;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.gms.dao.entity.table.AutobanConfigDOTableDef.AUTOBAN_CONFIG_D_O;

/**
 * 自动封禁配置服务。
 *
 * @author Nap
 * @since 2026-04-22
 */
@Service
@AllArgsConstructor
@Slf4j
public class AutobanConfigService {
    private final AutobanConfigMapper autobanConfigMapper;

    /**
     * 服务启动时加载配置到内存（初始化到 AutobanFactory）
     */
    public void loadConfigs() {
        List<AutobanConfigDO> configs = autobanConfigMapper.selectAll();

        // 转换为 Map
        Map<String, AutobanConfigDO> configMap = new HashMap<>();
        for (AutobanConfigDO config : configs) {
            configMap.put(config.getType(), config);
        }

        // 初始化到 AutobanFactory
        AutobanFactory.initConfig(configMap);
    }

    /**
     * 获取所有配置列表（包含枚举默认值）
     */
    public List<AutobanConfigDTO> getConfigList() {
        List<AutobanConfigDTO> result = new ArrayList<>();
        // 遍历所有枚举类型
        for (AutobanFactory factory : AutobanFactory.values()) {
            String type = factory.name();
            AutobanConfigDO config = AutobanFactory.getConfig(type);

            // 将毫秒转换为秒（-1表示永不过期，保持不变）
            long defaultExpireMs = factory.getExpire();
            Long defaultExpireSeconds = defaultExpireMs == -1 ? -1L : TimeUnit.MILLISECONDS.toSeconds(defaultExpireMs);

            AutobanConfigDTO dto = AutobanConfigDTO.builder()
                    .type(type)
                    .name(factory.getName())
                    .defaultPoints(factory.getMaximum())
                    .defaultExpireTimeSeconds(defaultExpireSeconds)
                    .build();

            if (config != null) {
                dto.setId(config.getId());
                dto.setDisabled(config.getDisabled());
                dto.setPoints(config.getPoints());
                // 将毫秒转换为秒
                if (config.getExpireTime() != null) {
                    dto.setExpireTimeSeconds(config.getExpireTime() == -1 ? -1L : TimeUnit.MILLISECONDS.toSeconds(config.getExpireTime()));
                }
                dto.setDescription(config.getDescription());
                // 前端根据 points/expireTime 是否为 null 来决定勾选状态
                dto.setChangePoints(config.getPoints() != null);
                dto.setChangeExpireTime(config.getExpireTime() != null);
            } else {
                dto.setDisabled(false);
                dto.setChangePoints(false);
                dto.setChangeExpireTime(false);
            }

            result.add(dto);
        }
        return result;
    }

    /**
     * 更新配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AutobanConfigDTO dto) {
        RequireUtil.requireNotEmpty(dto.getType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "type"));

        String type = dto.getType();
        AutobanConfigDO existing = autobanConfigMapper.selectOneByQuery(
                QueryWrapper.create().where(AUTOBAN_CONFIG_D_O.TYPE.eq(type))
        );

        // 构建 points 和 expireTime
        Integer points = dto.isChangePoints() ? dto.getPoints() : null;
        // 将秒转换为毫秒（-1表示永不过期，保持不变）
        Long expireTime = null;
        if (dto.isChangeExpireTime() && dto.getExpireTimeSeconds() != null) {
            expireTime = dto.getExpireTimeSeconds() == -1 ? -1L : TimeUnit.SECONDS.toMillis(dto.getExpireTimeSeconds());
        }
        Boolean disabled = dto.getDisabled() != null ? dto.getDisabled() : false;

        if (existing == null) {
            // 如果没有配置需要保存，则不需要插入记录
            if (points == null && expireTime == null && !disabled && (dto.getDescription() == null || dto.getDescription().isEmpty())) {
                // 从缓存中移除（如果存在）
                AutobanFactory.updateConfig(type, null);
                return;
            }
            // 插入新记录
            AutobanConfigDO newConfig = AutobanConfigDO.builder()
                    .type(type)
                    .disabled(disabled)
                    .points(points)
                    .expireTime(expireTime)
                    .description(dto.getDescription())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            autobanConfigMapper.insertSelective(newConfig);
            // 更新 DTO 的 id
            dto.setId(newConfig.getId());
            // 更新 AutobanFactory 缓存
            AutobanFactory.updateConfig(type, newConfig);
        } else {
            // 更新现有记录
            AutobanConfigDO updateConfig = AutobanConfigDO.builder()
                    .id(existing.getId())
                    .disabled(disabled)
                    .points(points)
                    .expireTime(expireTime)
                    .description(dto.getDescription())
                    .updateTime(new Date())
                    .build();
            autobanConfigMapper.update(updateConfig);
            // 更新 DTO 的 id
            dto.setId(existing.getId());
            // 更新 AutobanFactory 缓存（用 existing 对象更新后放入缓存）
            existing.setDisabled(disabled);
            existing.setPoints(points);
            existing.setExpireTime(expireTime);
            existing.setDescription(dto.getDescription());
            existing.setUpdateTime(new Date());
            AutobanFactory.updateConfig(type, existing);
        }
    }
}
