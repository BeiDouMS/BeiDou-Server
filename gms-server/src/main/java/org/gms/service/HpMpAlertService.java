package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.dao.mapper.HpMpAlertMapper;
import org.gms.dao.entity.HpMpAlertDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class HpMpAlertService {
    @Autowired
    private HpMpAlertMapper hpMpAlertMapper;
    public static final HashMap<Integer, HpMpAlertDO> cacheMap = new HashMap<>();

    public byte getHpAlert(int characterId) {
        byte result = 0;
        if (cacheMap.containsKey(characterId)) {
            result = cacheMap.get(characterId).getHp();
        } else {
            HpMpAlertDO hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                result = hpMpAlert.getHp();
                cacheMap.put(characterId, hpMpAlert);
            }
        }

        return result;
    }

    public void setHpAlert(int characterId, byte alert) {
        if (cacheMap.containsKey(characterId)) {
            cacheMap.get(characterId).setHp(alert);
        } else {
            HpMpAlertDO hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                hpMpAlert.setHp(alert);
            } else {
                hpMpAlert = HpMpAlertDO.builder().cId(characterId).hp(alert).mp((byte) 10).build();
            }
            cacheMap.put(characterId, hpMpAlert);
        }
    }

    public float getHpAlertPer(int characterId) {
        return (float) getHpAlert(characterId) / 20;
    }

    public byte getMpAlert(int characterId) {
        byte result = 0;
        if (cacheMap.containsKey(characterId)) {
            result = cacheMap.get(characterId).getMp();
        } else {
            HpMpAlertDO hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                result = hpMpAlert.getMp();
                cacheMap.put(characterId, hpMpAlert);
            }
        }

        return result;
    }

    public void setMpAlert(int characterId, byte alert) {
        if (cacheMap.containsKey(characterId)) {
            cacheMap.get(characterId).setMp(alert);
        } else {
            HpMpAlertDO hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                hpMpAlert.setMp(alert);
            } else {
                hpMpAlert = HpMpAlertDO.builder().cId(characterId).hp((byte) 10).mp(alert).build();
            }
            cacheMap.put(characterId, hpMpAlert);
        }
    }

    public float getMpAlertPer(int characterId) {
        return (float) getMpAlert(characterId) / 20;
    }

    /**
     * 保存缓存数据到数据库，目前仅在saveall命令和关闭服务器时调用
     */
    public void saveAll() {
        for (int id : cacheMap.keySet()) {
            hpMpAlertMapper.insertOrUpdate(cacheMap.get(id));
        }
        log.info("已保存 Hp Mp 警戒线到数据库");
    }

    /**
     * 清除缓存，目前仅在关闭服务器函数中被调用（重启时用）
     */
    public void clear() {
        cacheMap.clear();
    }
}
