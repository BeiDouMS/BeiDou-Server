package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import lombok.AllArgsConstructor;
import org.gms.client.Character;
import org.gms.client.inventory.*;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.InventoryequipmentDO;
import org.gms.dao.entity.InventoryitemsDO;
import org.gms.dao.mapper.*;
import org.gms.exception.BizException;
import org.gms.model.dto.*;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.CashIdGenerator;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.mybatisflex.core.query.QueryMethods.distinct;
import static org.gms.dao.entity.table.CharactersDOTableDef.CHARACTERS_D_O;
import static org.gms.dao.entity.table.InventoryequipmentDOTableDef.INVENTORYEQUIPMENT_D_O;
import static org.gms.dao.entity.table.InventoryitemsDOTableDef.INVENTORYITEMS_D_O;

@Transactional
@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryitemsMapper inventoryitemsMapper;
    private final InventoryequipmentMapper inventoryequipmentMapper;
    private final RingsMapper ringsMapper;
    private final PetsMapper petsMapper;

    public List<InventoryTypeRtnDTO> getInventoryTypeList() {
        List<InventoryTypeRtnDTO> list = new ArrayList<>();
        for (InventoryType value : InventoryType.values()) {
            list.add(InventoryTypeRtnDTO.builder().inventoryType(value.getType()).name(value.getName()).build());
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
        if (!RequireUtil.isEmpty(data.getCharacterName()))
            queryWrapper.and(CHARACTERS_D_O.NAME.like(data.getCharacterName()));
        Page<CharactersDO> paginate = inventoryitemsMapper.paginateAs(data.getPageNo(), data.getPageSize(), queryWrapper, CharactersDO.class);
        return new Page<>(
                paginate.getRecords().stream()
                        .map(record -> {
                            InventorySearchReqDTO dto = new InventorySearchReqDTO();
                            dto.setCharacterId(record.getId());
                            dto.setCharacterName(record.getName());
                            return dto;
                        })
                        .toList(),
                paginate.getPageNumber(),
                paginate.getPageSize(),
                paginate.getTotalRow()
        );
    }

    public List<InventorySearchRtnDTO> getInventoryList(InventorySearchReqDTO data) {
        RequireUtil.requireNotEmpty(data.getInventoryType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "inventoryType"));
        RequireUtil.requireNotNull(data.getCharacterId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "characterId"));
        InventoryType inventoryType = InventoryType.getByType(data.getInventoryType());
        RequireUtil.requireNotNull(inventoryType, I18nUtil.getExceptionMessage("UNKNOWN_PARAMETER_VALUE", "inventoryType", data.getInventoryType()));
        List<Row> results = inventoryitemsMapper.selectListByQueryAs(QueryWrapper.create()
                .select(INVENTORYITEMS_D_O.ALL_COLUMNS, INVENTORYEQUIPMENT_D_O.ALL_COLUMNS)
                .from(INVENTORYITEMS_D_O.as("i"))
                .leftJoin(INVENTORYEQUIPMENT_D_O.as("e")).on(INVENTORYITEMS_D_O.INVENTORYITEMID.eq(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID))
                // 只查询指定栏目
                .where(INVENTORYITEMS_D_O.INVENTORYTYPE.eq(data.getInventoryType()))
                // 只查询背包
                .and(INVENTORYITEMS_D_O.TYPE.eq(ItemFactory.INVENTORY.getValue()))
                .and(INVENTORYITEMS_D_O.CHARACTERID.eq(data.getCharacterId())), Row.class);
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteInventoryByCharacterId(int cid) {
        QueryWrapper itemQueryWrapper = QueryWrapper.create().where(INVENTORYITEMS_D_O.CHARACTERID.eq(cid));
        List<InventoryitemsDO> inventoryItemsDOS = inventoryitemsMapper.selectListByQuery(itemQueryWrapper);
        List<Long> inventoryItemIds = inventoryItemsDOS.stream().map(InventoryitemsDO::getInventoryitemid).toList();
        if (inventoryItemIds.isEmpty()) {
            return;
        }
        List<Integer> petIds = inventoryItemsDOS.stream()
                .map(InventoryitemsDO::getPetid)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!petIds.isEmpty()) {
            petsMapper.deleteBatchByIds(petIds);
            petIds.forEach(CashIdGenerator::freeCashId);
        }

        QueryWrapper equipmentQueryWrapper = QueryWrapper.create().where(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID.in(inventoryItemIds));
        List<InventoryequipmentDO> inventoryEquipmentDOS = inventoryequipmentMapper.selectListByQuery(equipmentQueryWrapper);
        List<Integer> ringIds = inventoryEquipmentDOS.stream()
                .map(InventoryequipmentDO::getRingid)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!ringIds.isEmpty()) {
            ringsMapper.deleteBatchByIds(ringIds);
            ringIds.forEach(CashIdGenerator::freeCashId);
        }
        inventoryequipmentMapper.deleteByQuery(equipmentQueryWrapper);
        inventoryitemsMapper.deleteByQuery(itemQueryWrapper);
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
            if (type.isEquip()) {
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

    @Transactional(rollbackFor = Exception.class)
    public void updateInventory(InventorySearchRtnDTO data) {
        modifyInventoryCheck(data);

        Character character = getCharacterById(data.getCharacterId());
        // 如果当前的玩家在线状态已经发生改变
        boolean isOnlineNow = character != null;
        if (isOnlineNow != data.isOnline()) {
            throw new BizException(I18nUtil.getExceptionMessage("InventoryService.updateInventory.exception1"));
        }
        if (isOnlineNow) {
            updateOnline(data, character);
        } else {
            updateDb(data);
        }
    }

    private void updateOnline(InventorySearchRtnDTO data, Character character) {
        InventoryType type = InventoryType.getByType(data.getInventoryType());
        Inventory inventory = character.getInventory(type);
        Item item = getModifyItem(data, inventory);

        // 仅以下值可修改
        if (data.getQuantity() != null && !type.isEquip()) item.setQuantity(data.getQuantity());
        if (data.getExpiration() != null) item.setExpiration(data.getExpiration());
        InventoryEquipRtnDTO equipment = data.getInventoryEquipment();
        if (type.isEquip() && equipment != null) {
            Equip equip = (Equip) item;
            if (equipment.getUpgradeSlots() != null) equip.setUpgradeSlots(equipment.getUpgradeSlots());
            if (equipment.getLevel() != null) equip.setLevel(equipment.getLevel());
            if (equipment.getAttStr() != null) equip.setStr(equipment.getAttStr());
            if (equipment.getAttDex() != null) equip.setDex(equipment.getAttDex());
            if (equipment.getAttInt() != null) equip.setInt(equipment.getAttInt());
            if (equipment.getAttLuk() != null) equip.setLuk(equipment.getAttLuk());
            if (equipment.getHp() != null) equip.setHp(equipment.getHp());
            if (equipment.getMp() != null) equip.setMp(equipment.getMp());
            if (equipment.getPAtk() != null) equip.setWatk(equipment.getPAtk());
            if (equipment.getMAtk() != null) equip.setMatk(equipment.getMAtk());
            if (equipment.getPDef() != null) equip.setWdef(equipment.getPDef());
            if (equipment.getMdef() != null) equip.setMdef(equipment.getMdef());
            if (equipment.getAcc() != null) equip.setAcc(equipment.getAcc());
            if (equipment.getAvoid() != null) equip.setAvoid(equipment.getAvoid());
            if (equipment.getHands() != null) equip.setHands(equipment.getHands());
            if (equipment.getSpeed() != null) equip.setSpeed(equipment.getSpeed());
            if (equipment.getJump() != null) equip.setJump(equipment.getJump());
            if (equipment.getVicious() != null) equip.setVicious(equipment.getVicious());
        }
        character.sendPacket(PacketCreator.modifyInventory(true, Arrays.asList(new ModifyInventory(3, item), new ModifyInventory(0, item))));
    }

    private void updateDb(InventorySearchRtnDTO data) {
        // 仅以下值可修改
        InventoryType type = InventoryType.getByType(data.getInventoryType());
        if (type.isEquip()) {
            InventoryEquipRtnDTO equipment = data.getInventoryEquipment();
            inventoryequipmentMapper.update(InventoryequipmentDO.builder()
                    .upgradeslots(equipment.getUpgradeSlots().intValue())
                    .level(equipment.getLevel().intValue())
                    .str(equipment.getAttStr().intValue())
                    .dex(equipment.getAttDex().intValue())
                    .inte(equipment.getAttInt().intValue())
                    .luk(equipment.getAttLuk().intValue())
                    .hp(equipment.getHp().intValue())
                    .mp(equipment.getMp().intValue())
                    .watk(equipment.getPAtk().intValue())
                    .matk(equipment.getMAtk().intValue())
                    .wdef(equipment.getPDef().intValue())
                    .mdef(equipment.getMdef().intValue())
                    .acc(equipment.getAcc().intValue())
                    .avoid(equipment.getAvoid().intValue())
                    .hands(equipment.getHands().intValue())
                    .speed(equipment.getSpeed().intValue())
                    .jump(equipment.getJump().intValue())
                    .vicious(equipment.getVicious().intValue())
                    .build());
        }
        inventoryitemsMapper.update(InventoryitemsDO.builder()
                .quantity(data.getQuantity().intValue())
                .expiration(data.getExpiration())
                .build());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteInventory(InventorySearchRtnDTO data) {
        modifyInventoryCheck(data);

        Character character = getCharacterById(data.getCharacterId());
        boolean isOnlineNow = character != null;
        // 如果当前的玩家在线状态已经发生改变
        if (isOnlineNow != data.isOnline()) {
            throw new BizException(I18nUtil.getExceptionMessage("InventoryService.deleteInventory.exception1"));
        }
        if (isOnlineNow) {
            InventoryType type = InventoryType.getByType(data.getInventoryType());
            Inventory inventory = character.getInventory(type);
            Item item = getModifyItem(data, inventory);

            //删除相对应的物品
            inventory.removeSlot(item.getPosition());
            character.sendPacket(PacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, item))));
        } else {
            QueryWrapper itemQueryWrapper = QueryWrapper.create()
                    .where(INVENTORYITEMS_D_O.CHARACTERID.eq(data.getCharacterId()))
                    .and(INVENTORYITEMS_D_O.ITEMID.eq(data.getItemId()))
                    .and(INVENTORYITEMS_D_O.POSITION.eq(data.getPosition()))
                    .and(INVENTORYITEMS_D_O.INVENTORYTYPE.eq(data.getInventoryType()));
            InventoryitemsDO inventoryItemsDOS = inventoryitemsMapper.selectOneByQuery(itemQueryWrapper);
            inventoryitemsMapper.deleteByQuery(QueryWrapper.create().where(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID.eq(inventoryItemsDOS.getInventoryitemid())));
            inventoryequipmentMapper.deleteById(inventoryItemsDOS.getInventoryitemid());
        }
    }

    private void modifyInventoryCheck(InventorySearchRtnDTO data) {
        RequireUtil.requireNotEmpty(data.getItemId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "itemId"));
        RequireUtil.requireNotEmpty(data.getInventoryType(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "inventoryType"));
        RequireUtil.requireNotNull(data.getCharacterId(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "characterId"));
        RequireUtil.requireNotNull(data.getPosition(), I18nUtil.getExceptionMessage("PARAMETER_SHOULD_NOT_EMPTY", "position"));
        InventoryType inventoryType = InventoryType.getByType(data.getInventoryType());
        RequireUtil.requireNotNull(inventoryType, I18nUtil.getExceptionMessage("UNKNOWN_PARAMETER_VALUE", "inventoryType", data.getInventoryType()));
    }

    private Item getModifyItem(InventorySearchRtnDTO data, Inventory inventory) {
        Item item = inventory.getItem(data.getPosition());
        RequireUtil.requireNotNull(item, I18nUtil.getExceptionMessage("InventoryService.updateInventory.exception2"));
        if (!Objects.equals(data.getItemId(), item.getItemId())) {
            throw new BizException(I18nUtil.getExceptionMessage("InventoryService.updateInventory.exception2"));
        }
        return item;
    }
}

