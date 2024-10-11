package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.dao.entity.PlayernpcsFieldDO;
import org.gms.dao.mapper.PlayernpcsFieldMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NpcService {
    private final PlayernpcsFieldMapper playernpcsFieldMapper;

    public List<PlayernpcsFieldDO> getPlayerNpcFields(PlayernpcsFieldDO condition) {
        QueryWrapper queryWrapper = QueryWrapper.create(condition);
        return playernpcsFieldMapper.selectListByQuery(queryWrapper);
    }
}
