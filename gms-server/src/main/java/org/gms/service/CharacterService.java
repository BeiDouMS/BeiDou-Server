package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.DefaultDates;
import org.gms.config.YamlConfig;
import org.gms.constants.string.ExtendType;
import org.gms.dao.entity.*;
import org.gms.dao.mapper.*;
import org.gms.dto.ChrOnlineListReqDTO;
import org.gms.dto.ChrOnlineListRtnDTO;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.BasePageUtil;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

import static org.gms.dao.entity.table.CharactersDOTableDef.CHARACTERS_D_O;
import static org.gms.dao.entity.table.ExtendValueDOTableDef.EXTEND_VALUE_D_O;
import static org.gms.dao.entity.table.RingsDOTableDef.RINGS_D_O;

@Service
@AllArgsConstructor
@Slf4j
public class CharacterService {
    private final ExtendValueMapper extendValueMapper;
    private final CharactersMapper charactersMapper;
    private final RingsMapper ringsMapper;
    private final NamechangesMapper namechangesMapper;
    private final AccountService accountService;
    private final BuddiesMapper buddiesMapper;
    private final WorldtransfersMapper worldtransfersMapper;

    public Page<ChrOnlineListRtnDTO> getChrOnlineList(ChrOnlineListReqDTO request) {
        Collection<Character> chrList = Server.getInstance().getWorld(request.getWorld()).getPlayerStorage().getAllCharacters();
        return BasePageUtil.create(chrList, request)
                .filter(chr -> Objects.isNull(request.getId()) || Objects.equals(chr.getId(), request.getId())
                        || RequireUtil.isEmpty(request.getName()) || Objects.equals(chr.getName(), request.getName())
                        || Objects.isNull(request.getMap()) || Objects.equals(chr.getMap().getId(), request.getMap()))
                .page(chr -> ChrOnlineListRtnDTO.builder()
                        .id(chr.getId())
                        .name(chr.getName())
                        .map(chr.getMap().getId())
                        .job(chr.getJob().getId())
                        .level(chr.getLevel())
                        .gm(chr.gmLevel())
                        .build());
    }

    public void updateRate(ExtendValueDO data) {
        checkName(data);
        data.setExtendType(ExtendType.CHARACTER_EXTEND.getType());
        data.setCreateTime(null);
        data.setUpdateTime(new Date(System.currentTimeMillis()));
        extendValueMapper.insertOrUpdateSelective(data);
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    public void resetRate(ExtendValueDO data) {
        checkName(data);
        extendValueMapper.deleteByQuery(QueryWrapper.create()
                .where(EXTEND_VALUE_D_O.EXTEND_ID.eq(data.getExtendId()))
                .and(EXTEND_VALUE_D_O.EXTEND_TYPE.eq(ExtendType.CHARACTER_EXTEND.getType()))
                .and(EXTEND_VALUE_D_O.EXTEND_NAME.eq(data.getExtendName())));
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    public void resetRates(ExtendValueDO data) {
        extendValueMapper.deleteByQuery(QueryWrapper.create()
                .where(EXTEND_VALUE_D_O.EXTEND_ID.eq(data.getExtendId()))
                .and(EXTEND_VALUE_D_O.EXTEND_TYPE.eq(ExtendType.CHARACTER_EXTEND.getType()))
                .and(EXTEND_VALUE_D_O.EXTEND_NAME.in("expRate", "dropRate", "mesoRate")));
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    /**
     * 改名
     *
     * @param data 改名对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void doNameChange(NamechangesDO data) {
        charactersMapper.update(CharactersDO.builder().id(data.getCharacterid()).name(data.getNewer()).build());
        ringsMapper.updateByQuery(RingsDO.builder().partnername(data.getNewer()).build(), QueryWrapper.create().where(RINGS_D_O.PARTNERNAME.eq(data.getOlder())));
        namechangesMapper.update(NamechangesDO.builder().id(data.getId()).completionTime(new Timestamp(System.currentTimeMillis())).build());
        log.info(I18nUtil.getLogMessage("CharacterService.doNameChange.info1"), data.getOlder(), data.getNewer());
    }

    /**
     * 校验能否转区
     *
     * @param data 转区对象
     * @return 能否转区
     */
    public boolean checkWorldTransferEligibility(WorldtransfersDO data) {
        if (!YamlConfig.config.server.ALLOW_CASHSHOP_WORLD_TRANSFER) {
            return false;
        }
        // 获取人物信息
        CharactersDO charactersDO = charactersMapper.selectOneById(data.getCharacterid());
        if (charactersDO == null) {
            return false;
        }
        // 判断是否结婚
        if (charactersDO.getPartnerId() != null) {
            return false;
        }
        // 判断是否被封禁
        AccountsDO accountsDO = accountService.findById(charactersDO.getAccountid());
        if (accountsDO == null) {
            return false;
        }
        if (accountsDO.getBanned() != null && accountsDO.getBanned()) {
            return false;
        }
        if (accountsDO.getTempban() != null && !Objects.equals(accountsDO.getTempban().toLocalDateTime(), DefaultDates.getTempban())) {
            return false;
        }
        // 判断名字是否被占用
        long count = charactersMapper.selectCountByQuery(QueryWrapper.create(CHARACTERS_D_O)
                .select()
                .where(CHARACTERS_D_O.NAME.eq(charactersDO.getName()))
                .and(CHARACTERS_D_O.WORLD.eq(data.getTo())));
        if (count > 0) {
            return false;
        }
        // 判断大区是否存在，万一被删了
        return Server.getInstance().getWorld(data.getTo()) != null;
    }

    /**
     * 转区
     *
     * @param data 转区对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void doWorldTransfer(WorldtransfersDO data) {
        // 获取人物信息
        CharactersDO charactersDO = charactersMapper.selectOneById(data.getCharacterid());
        RequireUtil.requireNotNull(charactersDO, I18nUtil.getLogMessage("UNKNOWN_CHARACTER"));
        charactersMapper.update(CharactersDO.builder()
                .id(charactersDO.getId())
                .world(data.getTo())
                .meso(Math.min(charactersDO.getMeso(), 1000000))
                .guildid(0L)
                .guildrank(0L)
                .build());
        buddiesMapper.delete(BuddiesDO.builder().characterid(charactersDO.getId()).build());
        buddiesMapper.delete(BuddiesDO.builder().buddyid(charactersDO.getId()).build());
        worldtransfersMapper.update(WorldtransfersDO.builder().id(data.getId()).completionTime(new Timestamp(System.currentTimeMillis())).build());
        log.info(I18nUtil.getLogMessage("CharacterService.doWorldTransfer.info1"), data.getFrom(), data.getTo());
    }

    private void checkName(ExtendValueDO data) {
        // 非法请求篡改其他字段
        if ("expRate".equals(data.getExtendName()) || "dropRate".equals(data.getExtendName()) || "mesoRate".equals(data.getExtendName())) {
            return;
        }
        throw new IllegalArgumentException();
    }


    private Character getCharacter(ExtendValueDO data) {
        for (World world : Server.getInstance().getWorlds()) {
            for (Character character : world.getPlayerStorage().getAllCharacters()) {
                if (ExtendType.isAccount(data.getExtendType()) && Objects.equals(String.valueOf(character.getAccountID()), data.getExtendId())) {
                    return character;
                }

                if (ExtendType.isCharacter(data.getExtendType()) && Objects.equals(String.valueOf(character.getId()), data.getExtendId())) {
                    return character;
                }
            }
        }

        throw new IllegalArgumentException(I18nUtil.getExceptionMessage("CharacterService.getCharacter.exception1"));
    }
}
