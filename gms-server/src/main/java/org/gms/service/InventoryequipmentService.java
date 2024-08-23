package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.dao.entity.InventoryequipmentDO;
import org.gms.dao.mapper.InventoryequipmentMapper;
import org.gms.model.dto.InventoryequipmentReqDTO;
import org.gms.model.dto.InventoryequipmentRtnDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryequipmentService {

    @Autowired
    private InventoryequipmentMapper inventoryequipmentMapper;

    public Page<InventoryequipmentRtnDTO> getDropList(InventoryequipmentReqDTO data) {

        InventoryequipmentDO inventoryequipmentDO=new InventoryequipmentDO();
        if (data.getInventoryequipmentid() != null) inventoryequipmentDO.setInventoryequipmentid(data.getInventoryequipmentid());
        if (data.getInventoryitemid() != null) inventoryequipmentDO.setInventoryitemid(data.getInventoryitemid());

        // 使用分页对象查询数据
        Page<InventoryequipmentDO> paginate= inventoryequipmentMapper.paginate(data.getPageNo(), data.getPageSize(), QueryWrapper.create(inventoryequipmentDO));

        return  new  Page<>(
                paginate.getRecords().stream()
                        .map(record -> InventoryequipmentRtnDTO.builder()
                                .inventoryequipmentid(record.getInventoryequipmentid())
                                .inventoryitemid(record.getInventoryitemid())
                                .acc(record.getAcc())
                                .avoid(record.getAvoid())
                                .dex(record.getDex())
                                .hands(record.getHands())
                                .wdef(record.getWdef())
                                .watk(record.getWatk())
                                .str(record.getStr())
                                .itemexp(record.getItemexp())
                                .vicious(record.getVicious())
                                .hands(record.getHands())
                                .mp(record.getMp())
                                .hp(record.getHp())
                                .vicious(record.getVicious())
                                .build())
                        .toList(),
                paginate.getPageNumber(),
                paginate.getPageSize(),
                paginate.getTotalRow()
            );


    }
}