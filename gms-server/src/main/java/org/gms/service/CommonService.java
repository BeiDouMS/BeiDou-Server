package org.gms.service;


import lombok.extern.slf4j.Slf4j;
import org.gms.client.inventory.Equip;
import org.gms.exception.BizException;
import org.gms.model.dto.EquipmentInfoReqDTO;
import org.gms.model.dto.EquipmentInfoRtnDTO;
import org.gms.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommonService {

    @Autowired
    private ItemService itemService;

    public EquipmentInfoRtnDTO getEquipmentInfoByItemId(EquipmentInfoReqDTO submitData) {
        if (submitData.getId() == null) {
            throw new BizException(I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_NULL"));
        }
        try {
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
        } catch (Exception e) {
            throw e;
        }


    }
}
