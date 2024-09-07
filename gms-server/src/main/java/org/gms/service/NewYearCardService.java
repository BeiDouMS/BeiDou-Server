package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.client.Character;
import org.gms.model.pojo.NewYearCardRecord;
import org.gms.dao.entity.NewyearDO;
import org.gms.dao.mapper.NewyearMapper;
import org.gms.net.server.Server;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.gms.dao.entity.table.NewyearDOTableDef.NEWYEAR_D_O;

@Service
@AllArgsConstructor
public class NewYearCardService {
    private final NewyearMapper newyearMapper;

    public void startPendingNewYearCardRequests() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(NEWYEAR_D_O)
                .where(NEWYEAR_D_O.TIMERECEIVED.eq(0))
                .and(NEWYEAR_D_O.SENDERDISCARD.eq(0));
        List<NewyearDO> newyearDOList = newyearMapper.selectListByQuery(queryWrapper);
        for (NewyearDO newyearDO : newyearDOList) {
            NewYearCardRecord newYearCardRecord = new NewYearCardRecord(newyearDO.getSenderid(), newyearDO.getSendername(), newyearDO.getReceiverid(),
                    newyearDO.getReceivername(), newyearDO.getMessage());
            newYearCardRecord.setExtraNewYearCardRecord(newyearDO.getId().intValue(), newyearDO.getSenderdiscard(), newyearDO.getReceiverdiscard(),
                    newyearDO.getReceived(), newyearDO.getTimesent(), newyearDO.getTimereceived());
            Server.getInstance().setNewYearCard(newYearCardRecord);
            newYearCardRecord.startNewYearCardTask();
        }
    }

    public List<NewyearDO> loadPlayerNewYearCards(Character chr) {
        return newyearMapper.selectListByQuery(QueryWrapper.create().where(NEWYEAR_D_O.SENDERID.eq(chr.getId())).or(NEWYEAR_D_O.RECEIVERID.eq(chr.getId())));
    }
}
