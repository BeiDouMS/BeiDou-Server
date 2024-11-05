package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.NamechangesDO;
import org.gms.dao.entity.RingsDO;
import org.gms.dao.mapper.CharactersMapper;
import org.gms.dao.mapper.InventoryitemsMapper;
import org.gms.dao.mapper.NamechangesMapper;
import org.gms.dao.mapper.RingsMapper;
import org.gms.manager.ServerManager;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.gms.constants.id.ItemId;

import java.sql.Timestamp;
import java.util.List;

import static org.gms.dao.entity.table.InventoryitemsDOTableDef.INVENTORYITEMS_D_O;
import static org.gms.dao.entity.table.NamechangesDOTableDef.NAMECHANGES_D_O;
import static org.gms.dao.entity.table.RingsDOTableDef.RINGS_D_O;

@Service
@AllArgsConstructor
@Slf4j
public class NameChangeService {
    private final NamechangesMapper namechangesMapper;
    private final CharactersMapper charactersMapper;
    private final RingsMapper ringsMapper;
    private final InventoryitemsMapper inventoryitemsmapper;

    public void applyAllNameChange() {
        List<NamechangesDO> namechangesDOList = getAllNameChanges();
        namechangesDOList.forEach(namechangesDO -> {
            try {
                // 事物隔离
                ServerManager.getApplicationContext().getBean(NameChangeService.class).doNameChange(namechangesDO);
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("Server.init.error4"), e);
            }
        });
    }

    public void applyNameChange(int characterId, String characterName) {//下线时检测是否需要更换昵称并应用
        List<NamechangesDO> namechangesDOList = namechangesMapper.selectListByQuery(QueryWrapper.create()
                .where(NAMECHANGES_D_O.COMPLETION_TIME.isNull()).and(NAMECHANGES_D_O.CHARACTERID.eq(characterId)));
        if (!namechangesDOList.isEmpty()) {
            NamechangesDO namechangesDO = namechangesDOList.getFirst();
            try {
                ServerManager.getApplicationContext().getBean(NameChangeService.class).doNameChange(NamechangesDO.builder()
                        .id(namechangesDO.getId())
                        .characterid(characterId)
                        .older(characterName)
                        .newer(namechangesDO.getNewer())
                        .build());
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("Server.init.error4"), e);
            }
        }
    }

    public List<NamechangesDO> getAllNameChanges() {
        return namechangesMapper.selectListByQuery(QueryWrapper.create().where(NAMECHANGES_D_O.COMPLETION_TIME.isNull()));
    }

    /**
     * 改名
     *
     * @param data 改名对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void doNameChange(NamechangesDO data) {//应用昵称更改
        int accountid = charactersMapper.selectOneById(data.getCharacterid()).getAccountid();
        charactersMapper.update(CharactersDO.builder().id(data.getCharacterid()).name(data.getNewer()).build());
        ringsMapper.updateByQuery(RingsDO.builder().partnername(data.getNewer()).build(), QueryWrapper.create().where(RINGS_D_O.PARTNERNAME.eq(data.getOlder())));
        namechangesMapper.update(NamechangesDO.builder().id(data.getId()).completionTime(new Timestamp(System.currentTimeMillis())).build());
        //似乎没有修复使用改名卡撤销已修改名称的功能，当双击改名卡并确认撤销改名，客户端直接闪退报38错误，顾在此处一刀切，直接删除角色背包和账户商城里所有改名卡
        inventoryitemsmapper.deleteByQuery(QueryWrapper.create().where(INVENTORYITEMS_D_O.ITEMID.eq(ItemId.NAME_CHANGE)).and(INVENTORYITEMS_D_O.CHARACTERID.eq(data.getCharacterid()).or(INVENTORYITEMS_D_O.ACCOUNTID.eq(accountid))));
        log.info(I18nUtil.getLogMessage("CharacterService.doNameChange.info1"), data.getOlder(), data.getNewer());
    }

    public boolean registerNameChange(Character chr, String newName) {
        List<NamechangesDO> namechangesDOList = namechangesMapper.selectListByQuery(QueryWrapper.create()
                .where(NAMECHANGES_D_O.CHARACTERID.eq(chr.getId())));
        // 已有改名未生效或改名未冷却
        if (!namechangesDOList.isEmpty() && namechangesDOList.stream().anyMatch(namechangesDO ->
                namechangesDO.getCompletionTime() == null || namechangesDO.getCompletionTime().getTime() + GameConfig.getServerLong("name_change_cooldown") > System.currentTimeMillis())) {
            return false;
        }
        namechangesMapper.insertSelective(NamechangesDO.builder().characterid(chr.getId()).older(chr.getName()).newer(newName).build());
        return true;
    }

    public void cancelPendingNameChange(Character chr, boolean needFinish) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(NAMECHANGES_D_O.CHARACTERID.eq(chr.getId()));
        if (needFinish) queryWrapper.and(NAMECHANGES_D_O.COMPLETION_TIME.isNull());
        namechangesMapper.deleteByQuery(queryWrapper);
    }
}
