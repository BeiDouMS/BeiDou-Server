package org.gms.service;

import lombok.AllArgsConstructor;
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
}
