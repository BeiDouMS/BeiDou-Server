package org.gms.service;


import lombok.extern.slf4j.Slf4j;
import org.gms.client.BuffStat;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.Equip;
import org.gms.config.YamlConfig;
import org.gms.exception.BizException;
import org.gms.model.dto.ChrOnlineListReqDTO;
import org.gms.model.dto.EquipmentInfoReqDTO;
import org.gms.model.dto.EquipmentInfoRtnDTO;
import org.gms.net.server.Server;
import org.gms.util.BasePageUtil;
import org.gms.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CommonService {

    @Autowired
    private ItemService itemService;

    /**
     * 根据物品ID获取装备信息
     *
     * @param submitData 主要是装备的ID 物品的ID
     * @return EquipmentInfoRtnDTO
     */
    public EquipmentInfoRtnDTO getEquipmentInfoByItemId(EquipmentInfoReqDTO submitData) {
        if (submitData.getId() == null) {
            throw new BizException(I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL"));
        }
        Equip equip = itemService.getEquipmentInfoByItemId(submitData.getId());
        EquipmentInfoRtnDTO rtn = new EquipmentInfoRtnDTO();
        rtn.setStr(equip.getStr());
        rtn.setDex(equip.getDex());
        rtn.set_int(equip.getInt());
        rtn.setLuk(equip.getLuk());
        rtn.setHp(equip.getHp());
        rtn.setMp(equip.getMp());
        rtn.setPAtk(equip.getWatk());
        rtn.setMAtk(equip.getMatk());
        rtn.setPDef(equip.getWdef());
        rtn.setMDef(equip.getMdef());
        rtn.setAcc(equip.getAcc());
        rtn.setAvoid(equip.getAvoid());
        rtn.setHands(equip.getHands());
        rtn.setSpeed(equip.getSpeed());
        rtn.setJump(equip.getJump());
        rtn.setUpgradeSlot(equip.getUpgradeSlots());
        rtn.setExpire(equip.getExpiration());
        return rtn;
    }

    /**
     * 根据世界ID去获取当前世界在线玩家数量
     *
     * @param worldId 大区id
     * @return Integer 在线玩家数量
     */
    public Integer getOnlinePlayerCountByWorldId(Integer worldId) {
        if (worldId == null) {
            return 0;
        }
        //如果传参未序列化可能导致数据丢失Optional做兜底
        return Server.getInstance().getWorld(worldId).getPlayerStorage().getSize();
    }

    /**
     * 查询所有世界的在线玩家并加总
     * @param worldIdList 大区id
     * @return 在线玩家总数
     */
    public Integer getAllWorldsOnlinePlayersCount(List<Integer> worldIdList) {
        //使用Optional判断worldIdList,如果size为0,则给new一个,相当于就是给worldId赋值0
        if (worldIdList == null) worldIdList = new ArrayList<>();

        //直接创建好对应世界个数的集合大小,虽然扩容机制估计用不到,但万一呢
        return worldIdList.stream().map(this::getOnlinePlayerCountByWorldId).mapToInt(i -> i).sum();

    }


}
