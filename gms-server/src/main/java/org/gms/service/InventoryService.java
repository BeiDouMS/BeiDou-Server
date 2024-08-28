package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import lombok.AllArgsConstructor;
import org.gms.client.inventory.InventoryType;
import org.gms.dao.mapper.InventoryitemsMapper;
import org.gms.model.dto.InventoryEquipRtnDTO;
import org.gms.model.dto.InventorySearchRtnDTO;
import org.gms.model.dto.InventoryTypeRtnDTO;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.gms.dao.entity.table.InventoryequipmentDOTableDef.INVENTORYEQUIPMENT_D_O;
import static org.gms.dao.entity.table.InventoryitemsDOTableDef.INVENTORYITEMS_D_O;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryitemsMapper inventoryitemsMapper;

    public List<InventoryTypeRtnDTO> getInventoryTypeList() {
        List<InventoryTypeRtnDTO> list = new ArrayList<>();
        for (InventoryType value : InventoryType.values()) {
            list.add(InventoryTypeRtnDTO.builder().type(value.getType()).name(value.getName()).build());
        }
        return list;
    }

    public List<InventorySearchRtnDTO> getInventoryList(InventoryTypeRtnDTO data) {
        RequireUtil.requireNotEmpty(data.getType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "type"));
        List<Row> results = inventoryitemsMapper.selectListByQueryAs(QueryWrapper.create()
                .select(INVENTORYITEMS_D_O.ALL_COLUMNS, INVENTORYEQUIPMENT_D_O.ALL_COLUMNS)
                .from(INVENTORYITEMS_D_O.as("i"))
                .leftJoin(INVENTORYEQUIPMENT_D_O.as("e")).on(INVENTORYITEMS_D_O.INVENTORYITEMID.eq(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID))
                .where(INVENTORYITEMS_D_O.INVENTORYTYPE.eq(data.getType())), Row.class);
        // todo 获取在线玩家数据，覆盖数据库数据
        return results.stream().map(obj -> {
            InventorySearchRtnDTO rtnDTO = InventorySearchRtnDTO.builder()
                    .id(obj.getLong("inventoryitemid"))
                    .itemType(obj.getInt("type"))
                    .character(obj.getInt("characterid") != null)
                    .saverId(obj.getInt("characterid") != null ? obj.getInt("characterid") : obj.getInt("accountid"))
                    .itemId(obj.getInt("itemid"))
                    .inventoryType(obj.getInt("inventorytype"))
                    .position(obj.getInt("position"))
                    .quantity(obj.getInt("quantity"))
                    .owner(obj.getString("owner"))
                    .petId(obj.getInt("petid"))
                    .flag(obj.getInt("flag"))
                    .expiration(obj.getLong("expiration"))
                    .giftFrom(obj.getString("giftFrom"))
                    .build();
            Long inventoryEquipmentId = obj.getLong("inventoryequipmentid");
            if (inventoryEquipmentId != null) {
                rtnDTO.setEquipment(true);
                rtnDTO.setInventoryEquipment(InventoryEquipRtnDTO.builder()
                        .id(inventoryEquipmentId)
                        .inventoryItemId(obj.getLong("inventoryitemid"))
                        .upgradeSlots(obj.getInt("upgradeslots"))
                        .level(obj.getInt("level"))
                        .attStr(obj.getInt("str"))
                        .attDex(obj.getInt("dex"))
                        .attInt(obj.getInt("int"))
                        .attLuk(obj.getInt("luk"))
                        .hp(obj.getInt("hp"))
                        .mp(obj.getInt("mp"))
                        .pAtk(obj.getInt("watk"))
                        .mAtk(obj.getInt("matk"))
                        .pDef(obj.getInt("pdef"))
                        .mdef(obj.getInt("mdef"))
                        .acc(obj.getInt("acc"))
                        .avoid(obj.getInt("avoid"))
                        .hands(obj.getInt("hands"))
                        .speed(obj.getInt("speed"))
                        .jump(obj.getInt("jump"))
                        .locked(obj.getInt("locked"))
                        .vicious(obj.getLong("vicious"))
                        .itemLevel(obj.getInt("itemlevel"))
                        .itemExp(obj.getLong("itemexp"))
                        .ringId(obj.getInt("ringid"))
                        .build());
            }
            return rtnDTO;
        }).toList();
    }
}
