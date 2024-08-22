package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.gms.dao.entity.DropDataGlobalDO;
import org.gms.dao.entity.InventoryequipmentDO;
import org.gms.dao.mapper.InventoryequipmentMapper;
import org.gms.model.dto.BasePageDTO;
import org.gms.model.dto.DropSearchReqDTO;
import org.gms.model.dto.DropSearchRtnDTO;
import org.gms.model.dto.InventoryequipmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventoryequipmentService {

    @Autowired
    private InventoryequipmentMapper inventoryequipmentMapper;

    public List<InventoryequipmentDTO> getDropList(InventoryequipmentDTO data) {

        InventoryequipmentDO inventoryequipmentDO=new InventoryequipmentDO();
        if (data.getInventoryequipmentid() != null) inventoryequipmentDO.setInventoryequipmentid(data.getInventoryequipmentid());
        if (data.getInventoryitemid() != null) inventoryequipmentDO.setInventoryitemid(data.getInventoryitemid());
        if (data.getUpgradeslots()!=null) inventoryequipmentDO.setUpgradeslots(data.getUpgradeslots());

        // 使用分页对象查询数据
        Page<InventoryequipmentDO> paginate= inventoryequipmentMapper.paginate(data.getPageNo(), data.getPageSize(), QueryWrapper.create(inventoryequipmentDO));

         return paginate.getRecords().stream().map(InventoryequipmentDTO::new).toList();

    }
}