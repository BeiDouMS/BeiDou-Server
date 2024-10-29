package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.client.QuestStatus;
import org.gms.dao.entity.MedalmapsDO;
import org.gms.dao.entity.QuestprogressDO;
import org.gms.dao.entity.QueststatusDO;
import org.gms.dao.mapper.MedalmapsMapper;
import org.gms.dao.mapper.QuestprogressMapper;
import org.gms.dao.mapper.QueststatusMapper;
import org.gms.server.quest.Quest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.gms.dao.entity.table.MedalmapsDOTableDef.MEDALMAPS_D_O;
import static org.gms.dao.entity.table.QuestprogressDOTableDef.QUESTPROGRESS_D_O;
import static org.gms.dao.entity.table.QueststatusDOTableDef.QUESTSTATUS_D_O;

@Service
@AllArgsConstructor
public class QuestService {
    private final MedalmapsMapper medalmapsMapper;
    private final QuestprogressMapper questprogressMapper;
    private final QueststatusMapper queststatusMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestProgressByCharacter(int cid) {
        medalmapsMapper.deleteByQuery(QueryWrapper.create().where(MEDALMAPS_D_O.CHARACTERID.eq(cid)));
        questprogressMapper.deleteByQuery(QueryWrapper.create().where(QUESTPROGRESS_D_O.CHARACTERID.eq(cid)));
        queststatusMapper.deleteByQuery(QueryWrapper.create().where(QUESTSTATUS_D_O.CHARACTERID.eq(cid)));
    }

    public List<QuestStatus> getQuestStatusByCharacter(int cid) {
        List<QueststatusDO> queststatusDOList = queststatusMapper.selectListByQuery(QueryWrapper.create().where(QUESTSTATUS_D_O.CHARACTERID.eq(cid)));
        List<QuestprogressDO> questprogressDOList = questprogressMapper.selectListByQuery(QueryWrapper.create().where(QUESTPROGRESS_D_O.CHARACTERID.eq(cid)));
        List<MedalmapsDO> medalmapsDOList = medalmapsMapper.selectListByQuery(QueryWrapper.create().where(MEDALMAPS_D_O.CHARACTERID.eq(cid)));

        return queststatusDOList.stream().map(queststatusDO -> {
            Quest quest = Quest.getInstance(queststatusDO.getQuest());
            QuestStatus questStatus = new QuestStatus(quest, QuestStatus.Status.getById(queststatusDO.getStatus()));
            if (queststatusDO.getTime() > -1) {
                questStatus.setCompletionTime(TimeUnit.SECONDS.toMillis(queststatusDO.getTime()));
            }
            if (queststatusDO.getExpires() > 0) {
                questStatus.setExpirationTime(queststatusDO.getExpires());
            }
            questStatus.setForfeited(queststatusDO.getForfeited());
            questStatus.setCompleted(queststatusDO.getCompleted());
            questprogressDOList.stream()
                    .filter(questprogressDO -> Objects.equals(queststatusDO.getQueststatusid(), questprogressDO.getQueststatusid()))
                    .forEach(questprogressDO -> questStatus.setProgress(questprogressDO.getProgressid(),  questprogressDO.getProgress()));
            medalmapsDOList.stream()
                    .filter(medalmapsDO -> Objects.equals(queststatusDO.getQueststatusid(), medalmapsDO.getQueststatusid()))
                    .forEach(medalmapsDO -> questStatus.addMedalMap(medalmapsDO.getMapid()));
            return questStatus;
        }).toList();
    }
}
