package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.inventory.Item;
import org.gms.dao.entity.GachaponRewardDO;
import org.gms.dao.entity.GachaponRewardPoolDO;
import org.gms.dao.mapper.GachaponRewardMapper;
import org.gms.dao.mapper.GachaponRewardPoolMapper;
import org.gms.model.dto.GachaponPoolSearchReqDTO;
import org.gms.model.dto.GachaponPoolSearchRtnDTO;
import org.gms.net.server.Server;
import org.gms.server.ItemInformationProvider;
import org.gms.server.gachapon.Gachapon;
import org.gms.server.life.LifeFactory;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;
import org.gms.util.RequireUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class GachaponService {
    @Autowired
    private GachaponRewardPoolMapper gachaponRewardPoolMapper;
    @Autowired
    private GachaponRewardMapper gachaponRewardMapper;


    private static final HashMap<Integer, List<GachaponRewardDO>> poolRewardsCache = new HashMap<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static final Lock rLock = lock.readLock();
    private static final Lock wLock = lock.writeLock();


    public void updatePool(GachaponRewardPoolDO submit) {
        wLock.lock();
        try {
            RequireUtil.requireNotNull(submit.getStartTime(), "生效时间不能为空");
            if (submit.getIsPublic()) {
                submit.setGachaponId(-1);
                submit.setWeight(0);
                RequireUtil.requireNotNull(submit.getProb(), "公共奖池中奖率不能为空");
            } else {
                submit.setProb(0);
                RequireUtil.requireNotNull(submit.getGachaponId(), "百宝箱ID不能为空");
                RequireUtil.requireNotNull(submit.getWeight(), "奖池权重不能为空");
            }
            // 校验城镇概率和是否大于100%
            gachaponRewardPoolMapper.insertOrUpdate(submit);
            poolRewardsCache.remove(submit.getId());
        } finally {
            wLock.unlock();
        }
    }

    @Transactional
    public void deletePool(Integer id) {
        wLock.lock();
        try {
            gachaponRewardPoolMapper.deleteById(id);
            gachaponRewardMapper.deleteByQuery(QueryWrapper.create().where("pool_id=?", id));
            poolRewardsCache.remove(id);
        } finally {
            wLock.unlock();
        }
    }

    public Page<GachaponPoolSearchRtnDTO> getPools(GachaponPoolSearchReqDTO condition) {
        rLock.lock();
        try {
            // 数据查询
            QueryWrapper qw = new QueryWrapper().orderBy(GachaponRewardPoolDO::getIsPublic, false);
            if (condition.getGachaponId() != null) {
                qw.eq("gachapon_id", condition.getGachaponId()).or("is_public=1");
            }
            Page<GachaponRewardPoolDO> paginate = gachaponRewardPoolMapper.paginate(
                    condition.getPageNo(),
                    condition.getPageSize(),
                    qw);

            // 类型转换
            List<GachaponPoolSearchRtnDTO> records = new ArrayList<>();
            GachaponPoolSearchRtnDTO data;
            for (GachaponRewardPoolDO record : paginate.getRecords()) {
                data = new GachaponPoolSearchRtnDTO();
                data.setId(record.getId());
                data.setName(record.getName());
                data.setGachaponId(record.getGachaponId());
                data.setWeight(record.getWeight());
                data.setIsPublic(record.getIsPublic());
                data.setProb(record.getProb());
                data.setStartTime(record.getStartTime());
                data.setEndTime(record.getEndTime());
                data.setNotification(record.getNotification());
                data.setComment(record.getComment());
                data.setGachaponName(LifeFactory.getNPCName(record.getGachaponId()));
                records.add(data);
            }

            // 设置真实概率
            if (condition.getGachaponId() != null && condition.getGachaponId() != -1) {
                setRealProb(records);
            }

            return new Page<>(
                    records,
                    paginate.getPageNumber(),
                    paginate.getPageSize(),
                    paginate.getTotalRow()
            );
        } finally {
            rLock.unlock();
        }
    }

    public List<GachaponRewardDO> getRewards(Integer poolId) {
        rLock.lock();
        try {
            List<GachaponRewardDO> records = gachaponRewardMapper.selectListByQuery(QueryWrapper.create()
                    .eq("pool_id", poolId)
                    .orderBy(GachaponRewardDO::getItemId, true));
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (GachaponRewardDO record : records) {
                record.setItemName(ii.getName(record.getItemId()));
            }
            return records;
        } finally {
            rLock.unlock();
        }
    }

    public void updateReward(GachaponRewardDO reward) {
        wLock.lock();
        try {
            gachaponRewardMapper.insertOrUpdate(reward);
            poolRewardsCache.remove(reward.getPoolId());
        } finally {
            wLock.unlock();
        }
    }

    public void deleteReward(Integer id) {
        wLock.lock();
        try {
            GachaponRewardDO reward = gachaponRewardMapper.selectOneById(id);
            if (reward != null) {
                gachaponRewardMapper.deleteById(id);
                poolRewardsCache.remove(reward.getPoolId());
            }
        } finally {
            wLock.unlock();
        }
    }

    private void setRealProb(List<GachaponPoolSearchRtnDTO> pools) {
        int probTotal = pools.stream().mapToInt(GachaponPoolSearchRtnDTO::getProb).sum();
        int probPoint = 100 * probTotal;
        int weightPoint = 1000000 - probPoint;

        int totalWeight = pools.stream().mapToInt(GachaponPoolSearchRtnDTO::getWeight).sum(); // 总权重
        for (GachaponPoolSearchRtnDTO pool : pools) {
            if (pool.getIsPublic()) {
                pool.setRealProb(pool.getProb() * 100);
            } else {
                pool.setRealProb(Math.round((float) weightPoint * pool.getWeight() / totalWeight));
            }
        }
    }

    private List<GachaponRewardPoolDO> getActivePools(Integer gachaponId) {
        rLock.lock();
        try {
            // pools 存在有效期，不能缓存
            Timestamp now = new Timestamp(System.currentTimeMillis());

            return gachaponRewardPoolMapper.selectListByQuery(QueryWrapper.create()
                    .where("(gachapon_id=? or is_public=1)", gachaponId)
                    .where("(start_time is null or start_time<=?)", now)
                    .where("(end_time is null or end_time>=?)", now)
                    .orderBy("id")
            );
        } finally {
            rLock.unlock();
        }
    }

    public void doGachapon(Character player, int gachaponId) {
        rLock.lock();
        try {
            List<GachaponRewardPoolDO> pools = getActivePools(gachaponId); // 已按ID排序
            if (pools.isEmpty()) {
                player.message("百宝箱为空，请联系管理员，百宝箱id: " + gachaponId);
                log.error("百宝箱奖池为空，百宝箱id:{} 抽奖人:[{}] {}", gachaponId, player.getId(), player.getName());
                return;
            }

            int point; // 积分
            int pointTotal = 0; // 累计积分

            int probTotal = pools.stream().mapToInt(GachaponRewardPoolDO::getProb).sum();
            int probPoint = 100 * probTotal; // 公共奖池积分总额
            int weightPoint = 1000000 - probPoint; // 非公共奖池积分总额

            int totalWeight = pools.stream().mapToInt(GachaponRewardPoolDO::getWeight).sum(); // 总权重
            int random = Randomizer.nextInt(1000000); // 随机数
            GachaponRewardPoolDO target = null;
            for (GachaponRewardPoolDO pool : pools) {
                // 按权重分配积分
                if (pool.getIsPublic()) {
                    point = pool.getProb() * 100;
                } else {
                    point = Math.round((float) weightPoint * pool.getWeight() / totalWeight);
                }

                pointTotal += point;

                if (pointTotal > random) {
                    target = pool;
                    break;
                }
            }

            if (target == null) {
                // 如果三个奖池的权重分别是 8 8 2 / 3 3 3或其他类似的组合，那么有近乎于0（但不等于0）的概率出现null的情况
                target = pools.getFirst();
            }
            doReward(player, target);
        } finally {
            rLock.unlock();
        }
    }

    public List<GachaponRewardDO> getRewardsByNpcId(Integer npcId) {
        List<GachaponRewardPoolDO> activePools = getActivePools(npcId);
        return activePools.stream().flatMap(pool -> getRewards(pool.getId()).stream()).toList();
    }

    private void doReward(Character player, GachaponRewardPoolDO pool) {
        List<GachaponRewardDO> poolRewards = getPoolRewards(pool.getId());
        if (poolRewards.isEmpty()) {
            player.message("奖池为空，请联系管理员");
            log.error("百宝箱奖池为空，奖池id:{} 抽奖人:[{}] {}", pool.getId(), player.getId(), player.getName());
            return;
        }

        int random = Randomizer.nextInt(poolRewards.size());
        GachaponRewardDO reward = poolRewards.get(random);
        Item itemGained = player.getAbstractPlayerInteraction().gainItem(reward.getItemId(), reward.getQuantity(), true, true);
        // 修复背包满导致的空指针
        if (itemGained == null) {
            return;
        }
        String gachaponMessage = I18nUtil.getMessage("GachaMessage.message1",player.getMap().getMapName(),reward.getQuantity(),ItemInformationProvider.getInstance().getName(reward.getItemId()));
        player.dropMessage(gachaponMessage);
        Gachapon.log(player, reward.getItemId(), player.getMap().getMapName());

        if (pool.getNotification()) {
            Server.getInstance().broadcastMessage(player.getWorld(), PacketCreator.gachaponMessage(itemGained, player.getMap().getMapName(), player));
        }
    }

    private List<GachaponRewardDO> getPoolRewards(Integer poolId) {
        if (poolRewardsCache.containsKey(poolId)) {
            return poolRewardsCache.get(poolId);
        } else {
            List<GachaponRewardDO> poolRewards = gachaponRewardMapper.selectListByQuery(QueryWrapper.create().where("pool_id=?", poolId));
            poolRewardsCache.put(poolId, poolRewards);
            return poolRewards;
        }
    }
}
