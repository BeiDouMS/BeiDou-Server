package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.dao.entity.NxcouponsDO;
import org.gms.dao.mapper.NxcouponsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NxCouponService {
    private final NxcouponsMapper nxcouponsMapper;

    public List<Integer> selectActiveCouponIds(int weekDay, int hourDay) {
        return nxcouponsMapper.selectActiveCouponIds(weekDay, hourDay);
    }

    public List<NxcouponsDO> getNxCoupons(NxcouponsDO condition) {
        QueryWrapper queryWrapper = QueryWrapper.create(condition);
        return nxcouponsMapper.selectListByQuery(queryWrapper);
    }
}
