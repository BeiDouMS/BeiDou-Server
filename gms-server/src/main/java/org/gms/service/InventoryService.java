package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import lombok.AllArgsConstructor;
import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.ItemFactory;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.mapper.CharactersMapper;
import org.gms.dao.mapper.InventoryitemsMapper;
import org.gms.model.dto.*;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.mybatisflex.core.query.QueryMethods.distinct;
import static org.gms.dao.entity.table.CharactersDOTableDef.CHARACTERS_D_O;
import static org.gms.dao.entity.table.InventoryequipmentDOTableDef.INVENTORYEQUIPMENT_D_O;
import static org.gms.dao.entity.table.InventoryitemsDOTableDef.INVENTORYITEMS_D_O;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryitemsMapper inventoryitemsMapper;
    private final CharactersMapper charactersMapper;

    public List<InventoryTypeRtnDTO> getInventoryTypeList() {
        List<InventoryTypeRtnDTO> list = new ArrayList<>();
        for (InventoryType value : InventoryType.values()) {
            list.add(InventoryTypeRtnDTO.builder().type(value.getType()).name(value.getName()).build());
        }
        return list;
    }

    public Page<InventorySearchReqDTO> getCharacterList(InventorySearchReqDTO data) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(distinct(CHARACTERS_D_O.ID, CHARACTERS_D_O.NAME, CHARACTERS_D_O.ACCOUNTID))
                .from(INVENTORYITEMS_D_O.as("i"))
                .leftJoin(CHARACTERS_D_O.as("c")).on(INVENTORYITEMS_D_O.CHARACTERID.eq(CHARACTERS_D_O.ID))
                .where(INVENTORYITEMS_D_O.TYPE.eq(ItemFactory.INVENTORY.getValue()));
        if (data.getCharacterId() != null) queryWrapper.and(CHARACTERS_D_O.ID.eq(data.getCharacterId()));
        if (!RequireUtil.isEmpty(data.getCharacterName())) queryWrapper.and(CHARACTERS_D_O.NAME.like(data.getCharacterName()));
        if (data.getAccountId() != null) queryWrapper.and(CHARACTERS_D_O.ACCOUNTID.eq(data.getAccountId()));
        Page<CharactersDO> paginate = inventoryitemsMapper.paginateAs(data.getPageNo(), data.getPageSize(), queryWrapper, CharactersDO.class);
        return new Page<>(
                paginate.getRecords().stream()
                        .map(record -> {
                            InventorySearchReqDTO dto = new InventorySearchReqDTO();
                            dto.setCharacterId(record.getId());
                            dto.setCharacterName(record.getName());
                            dto.setAccountId(record.getAccountid());
                            return dto;
                        })
                        .toList(),
                paginate.getPageNumber(),
                paginate.getPageSize(),
                paginate.getTotalRow()
        );
    }

    public List<InventorySearchRtnDTO> getInventoryList(InventorySearchReqDTO data) {
        RequireUtil.requireNotEmpty(data.getInventoryType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "type"));
        InventoryType inventoryType = InventoryType.getByType(data.getInventoryType());
        RequireUtil.requireNotNull(inventoryType, I18nUtil.getExceptionMessage("UNKNOWN_PARAMETER_VALUE", "type", data.getInventoryType()));
        List<Row> results = inventoryitemsMapper.selectListByQueryAs(QueryWrapper.create()
                .select(INVENTORYITEMS_D_O.ALL_COLUMNS, INVENTORYEQUIPMENT_D_O.ALL_COLUMNS)
                .from(INVENTORYITEMS_D_O.as("i"))
                .leftJoin(INVENTORYEQUIPMENT_D_O.as("e")).on(INVENTORYITEMS_D_O.INVENTORYITEMID.eq(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID))
                // 只查询指定栏目
                .where(INVENTORYITEMS_D_O.INVENTORYTYPE.eq(data.getInventoryType()))
                // 只查询背包
                .and(INVENTORYITEMS_D_O.TYPE.eq(ItemFactory.INVENTORY.getValue())), Row.class);
        List<InventorySearchRtnDTO> rtnDTOList = new ArrayList<>();
        Set<Character> characterSet = new HashSet<>();
        for (Row obj : results) {
            int characterId = obj.getInt("characterid");
            Character character = getCharacterById(characterId);
            // 过滤在线玩家
            if (character == null) {
                rtnDTOList.add(buildByDb(obj));
            } else {
                characterSet.add(character);
            }
        }
        // 整合在线玩家数据
        for (Character character : characterSet) {
            rtnDTOList.addAll(buildByOnline(character, inventoryType));
        }
        return rtnDTOList;
    }

    private Character getCharacterById(int characterId) {
        for (World world : Server.getInstance().getWorlds()) {
            Optional<Character> characterOptional = world.getPlayerStorage().getAllCharacters().stream()
                    .filter(c -> Objects.equals(c.getId(), characterId))
                    .findFirst();
            if (characterOptional.isPresent()) {
                return characterOptional.get();
            }
        }
        return null;
    }

    private InventorySearchRtnDTO buildByDb(Row obj) {
        InventorySearchRtnDTO rtnDTO = InventorySearchRtnDTO.builder()
                .id(obj.getLong("inventoryitemid"))
                .itemType(obj.getInt("type"))
                .characterId(obj.getInt("characterid"))
                .itemId(obj.getInt("itemid"))
                .inventoryType(obj.getByte("inventorytype"))
                .position(obj.getShort("position"))
                .quantity(obj.getShort("quantity"))
                .owner(obj.getString("owner"))
                .petId(obj.getInt("petid"))
                .flag(obj.getShort("flag"))
                .expiration(obj.getLong("expiration"))
                .giftFrom(obj.getString("giftFrom"))
                .online(false)
                .build();
        Long inventoryEquipmentId = obj.getLong("inventoryequipmentid");
        if (inventoryEquipmentId != null) {
            rtnDTO.setEquipment(true);
            rtnDTO.setInventoryEquipment(InventoryEquipRtnDTO.builder()
                    .id(inventoryEquipmentId)
                    .inventoryItemId(obj.getLong("inventoryitemid"))
                    .upgradeSlots(obj.getByte("upgradeslots"))
                    .level(obj.getByte("level"))
                    .attStr(obj.getShort("str"))
                    .attDex(obj.getShort("dex"))
                    .attInt(obj.getShort("int"))
                    .attLuk(obj.getShort("luk"))
                    .hp(obj.getShort("hp"))
                    .mp(obj.getShort("mp"))
                    .pAtk(obj.getShort("watk"))
                    .mAtk(obj.getShort("matk"))
                    .pDef(obj.getShort("pdef"))
                    .mdef(obj.getShort("mdef"))
                    .acc(obj.getShort("acc"))
                    .avoid(obj.getShort("avoid"))
                    .hands(obj.getShort("hands"))
                    .speed(obj.getShort("speed"))
                    .jump(obj.getShort("jump"))
                    .locked(obj.getInt("locked"))
                    .vicious(obj.getShort("vicious"))
                    .itemLevel(obj.getByte("itemlevel"))
                    .itemExp(obj.getInt("itemexp"))
                    .ringId(obj.getInt("ringid"))
                    .build());
        }
        return rtnDTO;
    }

    private List<InventorySearchRtnDTO> buildByOnline(Character character, InventoryType type) {
        Inventory inventory = character.getInventory(type);
        return inventory.list().stream().map(item -> {
            InventorySearchRtnDTO rtnDTO = InventorySearchRtnDTO.builder()
                    .id(-1L)
                    .itemType(ItemFactory.INVENTORY.getValue())
                    .characterId(character.getId())
                    .itemId(item.getItemId())
                    .inventoryType(type.getType())
                    .position(item.getPosition())
                    .quantity(item.getQuantity())
                    .owner(item.getOwner())
                    .petId(item.getPetId())
                    .flag(item.getFlag())
                    .expiration(item.getExpiration())
                    .giftFrom(item.getGiftFrom())
                    .online(true)
                    .build();
            if (InventoryType.EQUIP == type || InventoryType.EQUIPPED == type) {
                Equip equip = (Equip) item;
                rtnDTO.setEquipment(true);
                rtnDTO.setInventoryEquipment(InventoryEquipRtnDTO.builder()
                        .id(-1L)
                        .inventoryItemId(-1L)
                        .upgradeSlots(equip.getUpgradeSlots())
                        .level(equip.getLevel())
                        .attStr(equip.getStr())
                        .attDex(equip.getDex())
                        .attInt(equip.getInt())
                        .attLuk(equip.getLuk())
                        .hp(equip.getHp())
                        .mp(equip.getMp())
                        .pAtk(equip.getWatk())
                        .mAtk(equip.getMatk())
                        .pDef(equip.getWdef())
                        .mdef(equip.getMdef())
                        .acc(equip.getAcc())
                        .avoid(equip.getAvoid())
                        .hands(equip.getHands())
                        .speed(equip.getSpeed())
                        .jump(equip.getJump())
                        .locked(0)
                        .vicious(equip.getVicious())
                        .itemLevel(equip.getItemLevel())
                        .itemExp(equip.getItemExp())
                        .ringId(equip.getRingId())
                        .build());
            }
            return rtnDTO;
        }).toList();
    }
}
