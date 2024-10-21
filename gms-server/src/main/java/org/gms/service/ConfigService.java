package org.gms.service;

import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.StringUtil;
import lombok.AllArgsConstructor;
import org.gms.dao.entity.GameConfigDO;
import org.gms.dao.entity.ServerPropDO;
import org.gms.dao.entity.WorldPropDO;
import org.gms.dao.mapper.GameConfigMapper;
import org.gms.dao.mapper.ServerPropMapper;
import org.gms.dao.mapper.WorldPropMapper;
import org.gms.property.ServerProperty;
import org.gms.property.WorldProperty;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.gms.dao.entity.table.WorldPropDOTableDef.WORLD_PROP_D_O;

@Service
@AllArgsConstructor
public class ConfigService {
    private final WorldPropMapper worldPropMapper;
    private final ServerPropMapper serverPropMapper;
    private final GameConfigMapper gameConfigMapper;

    public List<GameConfigDO> loadGameConfigs() {
        return gameConfigMapper.selectAll();
    }

    public List<WorldProperty.WorldsConfig> loadWorldProperty() {
        List<WorldPropDO> worldPropDOS = worldPropMapper.selectListByQuery(QueryWrapper.create()
                .where(WORLD_PROP_D_O.ENABLED.eq(1)));
        return worldPropDOS.stream().map(worldPropDO -> {
            WorldProperty.WorldsConfig worldsConfig = new WorldProperty.WorldsConfig();
            Field[] worldsConfigFields = worldsConfig.getClass().getDeclaredFields();
            Field[] worldPropFields = worldPropDO.getClass().getDeclaredFields();
            for (Field configField : worldsConfigFields) {
                configField.setAccessible(true);
                for (Field propField : worldPropFields) {
                    propField.setAccessible(true);
                    if (Objects.equals(StringUtil.camelToUnderline(propField.getName()).toUpperCase(), configField.getName().toUpperCase())) {
                        setValue(worldsConfig, configField, worldPropDO, propField);
                        break;
                    }
                }
            }
            return worldsConfig;
        }).collect(Collectors.toList());
    }

    public ServerProperty loadServerProperty() {
        List<ServerPropDO> serverPropDOS = serverPropMapper.selectAll();
        ServerProperty serverProperty = new ServerProperty();
        Field[] serverPropertyFields = serverProperty.getClass().getDeclaredFields();
        for (Field configField : serverPropertyFields) {
            configField.setAccessible(true);
            for (ServerPropDO serverPropDO : serverPropDOS) {
                if (Objects.equals(configField.getName().toUpperCase(), serverPropDO.getPropCode().toUpperCase())) {
                    setValue(serverProperty, configField, serverPropDO.getPropValue());
                    break;
                }
            }
        }
        return serverProperty;
    }

    private void setValue(Object dstObj, Field dstField, String value) {
        try {
            Object typeValue = value;
            if (dstField.getType() != String.class) {
                typeValue = JSONObject.parseObject(value, dstField.getType());
            }
            dstField.set(dstObj, typeValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValue(Object dstObj, Field dstField, Object srcObj, Field srcField) {
        try {
            dstField.set(dstObj, srcField.get(srcObj));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
