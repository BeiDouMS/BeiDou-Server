package service;

import com.mybatisflex.core.query.QueryWrapper;
import dao.entity.HpMpAlertEntity;
import dao.mapper.HpMpAlertMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class HpMpAlertService {
    @Autowired
    private HpMpAlertMapper hpMpAlertMapper;
    public static final HashMap<Integer, HpMpAlertEntity> cacheMap = new HashMap<>();

    public byte getHpAlert(int characterId) {
        byte result = 0;
        if (cacheMap.containsKey(characterId)) {
            result = cacheMap.get(characterId).getHp();
        } else {
            HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
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
            HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                hpMpAlert.setHp(alert);
            } else {
                hpMpAlert = HpMpAlertEntity.builder().cId(characterId).hp(alert).mp((byte) 10).build();
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
            HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
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
            HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
            if (hpMpAlert != null) {
                hpMpAlert.setMp(alert);
            } else {
                hpMpAlert = HpMpAlertEntity.builder().cId(characterId).hp((byte) 10).mp(alert).build();
            }
            cacheMap.put(characterId, hpMpAlert);
        }
    }

    public float getMpAlertPer(int characterId) {
        return (float) getMpAlert(characterId) / 20;
    }

    public void saveAll() {
        for (int id : cacheMap.keySet()) {
            hpMpAlertMapper.insertOrUpdate(cacheMap.get(id));
        }
        log.info("已保存 Hp Mp 警戒线到数据库");
    }

    public void clear() {
        cacheMap.clear();
    }
}
