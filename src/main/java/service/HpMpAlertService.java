package service;

import com.mybatisflex.core.query.QueryWrapper;
import dao.entity.HpMpAlertEntity;
import dao.mapper.HpMpAlertMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class HpMpAlertService {
    @Autowired
    private HpMpAlertMapper hpMpAlertMapper;
    public static final HashMap<Integer, HpMpAlertEntity> cacheMap = new HashMap<>();
    private static final Lock lock = new ReentrantLock(true);

    public byte getHpAlert(int characterId) {
        byte result = 0;
        lock.lock();
        try {
            if (cacheMap.containsKey(characterId)) {
                result = cacheMap.get(characterId).getHp();
            } else {
                HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
                if (hpMpAlert != null) {
                    result = hpMpAlert.getHp();
                    cacheMap.put(characterId, hpMpAlert);
                }
            }
        } finally {
            lock.unlock();
        }

        return result;
    }

    public void setHpAlert(int characterId, byte alert) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public float getHpAlertPer(int characterId) {
        return (float) getHpAlert(characterId) / 20;
    }

    public byte getMpAlert(int characterId) {
        byte result = 0;
        lock.lock();
        try {
            if (cacheMap.containsKey(characterId)) {
                result = cacheMap.get(characterId).getMp();
            } else {
                HpMpAlertEntity hpMpAlert = hpMpAlertMapper.selectOneByQuery(QueryWrapper.create().eq("c_id", characterId));
                if (hpMpAlert != null) {
                    result = hpMpAlert.getMp();
                    cacheMap.put(characterId, hpMpAlert);
                }
            }
        } finally {
            lock.unlock();
        }

        return result;
    }

    public void setMpAlert(int characterId, byte alert) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public float getMpAlertPer(int characterId) {
        return (float) getMpAlert(characterId) / 20;
    }

    public void saveAll() {
        lock.lock();
        try {
            for (int id : cacheMap.keySet()) {
                hpMpAlertMapper.insertOrUpdate(cacheMap.get(id));
            }
            log.info("已保存 Hp Mp 警戒线到数据库");
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            cacheMap.clear();
        } finally {
            lock.unlock();
        }
    }
}
