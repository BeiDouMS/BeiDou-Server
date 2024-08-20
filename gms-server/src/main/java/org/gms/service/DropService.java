package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.dao.entity.DropDataDO;
import org.gms.dao.entity.DropDataGlobalDO;
import org.gms.dao.mapper.DropDataGlobalMapper;
import org.gms.dao.mapper.DropDataMapper;
import org.gms.model.dto.DropSearchReqDTO;
import org.gms.model.dto.DropSearchRtnDTO;
import org.gms.server.ItemInformationProvider;
import org.gms.server.life.MonsterInformationProvider;
import org.gms.server.quest.Quest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DropService {
    private final DropDataMapper dropDataMapper;
    private final DropDataGlobalMapper dropDataGlobalMapper;

    public Page<DropSearchRtnDTO> getDropList(DropSearchReqDTO data, boolean isGlobal) {
        if (isGlobal) {
            DropDataGlobalDO dropDataGlobalDO = new DropDataGlobalDO();
            if (data.getContinent() != null) dropDataGlobalDO.setContinent(data.getContinent());
            if (data.getItemId() != null) dropDataGlobalDO.setItemid(data.getItemId());
            if (data.getQuestId() != null) dropDataGlobalDO.setQuestid(data.getQuestId());

            Page<DropDataGlobalDO> paginate = dropDataGlobalMapper.paginate(data.getPageNo(), data.getPageSize(), QueryWrapper.create(dropDataGlobalDO));
            return new Page<>(
                    paginate.getRecords().stream()
                            .map(record -> DropSearchRtnDTO.builder()
                                    .id(record.getId())
                                    .continent(record.getContinent())
                                    .itemId(record.getItemid())
                                    .itemName(getItemName(record.getItemid()))
                                    .minimumQuantity(record.getMinimumQuantity())
                                    .maximumQuantity(record.getMaximumQuantity())
                                    .questId(record.getQuestid())
                                    .questName(getQuestName(record.getQuestid()))
                                    .chance(record.getChance())
                                    .comments(record.getComments())
                                    .build())
                            .toList(),
                    paginate.getPageNumber(),
                    paginate.getPageSize(),
                    paginate.getTotalRow()
            );
        } else {
            DropDataDO dropDataDO = new DropDataDO();
            if (data.getDropperId() != null) dropDataDO.setDropperid(data.getDropperId());
            if (data.getItemId() != null) dropDataDO.setItemid(data.getItemId());
            if (data.getQuestId() != null) dropDataDO.setQuestid(data.getQuestId());

            Page<DropDataDO> paginate = dropDataMapper.paginate(data.getPageNo(), data.getPageSize(), QueryWrapper.create(dropDataDO));
            return new Page<>(
                    paginate.getRecords().stream()
                            .map(record -> DropSearchRtnDTO.builder()
                                    .id(record.getId())
                                    .dropperId(record.getDropperid())
                                    .dropperName(getMobName(record.getDropperid()))
                                    .itemId(record.getItemid())
                                    .itemName(getItemName(record.getItemid()))
                                    .minimumQuantity(record.getMinimumQuantity())
                                    .maximumQuantity(record.getMaximumQuantity())
                                    .questId(record.getQuestid())
                                    .questName(getQuestName(record.getQuestid()))
                                    .chance(record.getChance())
                                    .build())
                            .toList(),
                    paginate.getPageNumber(),
                    paginate.getPageSize(),
                    paginate.getTotalRow()
            );
        }
    }

    public Long modifyDropData(DropSearchRtnDTO data, boolean isGlobal, boolean isDelete) {
        Long dropDataId;
        if (isDelete) {
            (isGlobal ? dropDataGlobalMapper : dropDataMapper).deleteById(data.getId());
            dropDataId = data.getId();
        } else {
            if (isGlobal) {
                DropDataGlobalDO dropDataGlobalDO = DropDataGlobalDO.builder()
                        .id(data.getId())
                        .continent(data.getContinent())
                        .itemid(data.getItemId())
                        .minimumQuantity(data.getMinimumQuantity())
                        .maximumQuantity(data.getMaximumQuantity())
                        .questid(data.getQuestId())
                        .chance(data.getChance())
                        .comments(data.getComments())
                        .build();
                dropDataGlobalMapper.insertOrUpdate(dropDataGlobalDO, true);
                dropDataId = dropDataGlobalDO.getId();
            } else {
                DropDataDO dropDataDO = DropDataDO.builder()
                        .id(data.getId())
                        .dropperid(data.getDropperId())
                        .itemid(data.getItemId())
                        .minimumQuantity(data.getMinimumQuantity())
                        .maximumQuantity(data.getMaximumQuantity())
                        .questid(data.getQuestId())
                        .chance(data.getChance())
                        .build();
                dropDataMapper.insertOrUpdate(dropDataDO, true);
                dropDataId = dropDataDO.getId();
            }
        }
        MonsterInformationProvider.getInstance().clearDrops();
        return dropDataId;
    }

    private String getItemName(Integer itemId) {
        return itemId == null ? null : ItemInformationProvider.getInstance().getName(itemId);
    }

    private String getMobName(Integer mobId) {
        return mobId == null ? null : MonsterInformationProvider.getInstance().getMobNameFromId(mobId);
    }

    private String getQuestName(Integer questId) {
        return questId == null ? null : Quest.getInstance(questId).getName();
    }
}
