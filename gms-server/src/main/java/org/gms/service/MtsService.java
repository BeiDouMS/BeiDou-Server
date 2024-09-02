package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.dao.entity.MtsCartDO;
import org.gms.dao.mapper.MtsCartMapper;
import org.gms.dao.mapper.MtsItemsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.gms.dao.entity.table.MtsCartDOTableDef.MTS_CART_D_O;
import static org.gms.dao.entity.table.MtsItemsDOTableDef.MTS_ITEMS_D_O;

@Service
@AllArgsConstructor
public class MtsService {
    private final MtsCartMapper mtsCartMapper;
    private final MtsItemsMapper mtsItemsMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deleteMtsByCharacterId(int cid) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(MTS_CART_D_O.CID.eq(cid));
        List<MtsCartDO> mtsCartDOS = mtsCartMapper.selectListByQuery(queryWrapper);
        List<Integer> mtsIds = mtsCartDOS.stream().map(MtsCartDO::getId).toList();
        if (!mtsIds.isEmpty()) {
            mtsItemsMapper.deleteByQuery(QueryWrapper.create().where(MTS_ITEMS_D_O.ID.in(mtsIds)));
            mtsCartMapper.deleteByQuery(queryWrapper);
        }
    }
}
