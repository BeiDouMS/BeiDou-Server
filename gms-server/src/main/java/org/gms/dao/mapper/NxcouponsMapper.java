package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.gms.dao.entity.NxcouponsDO;

import java.util.List;

/**
 *  映射层。
 *
 * @author sleep
 * @since 2024-05-24
 */
public interface NxcouponsMapper extends BaseMapper<NxcouponsDO> {
    @Select("SELECT couponid FROM nxcoupons WHERE (activeday & #{weekDay}) = #{weekDay} AND starthour <= #{hourDay} AND endhour > #{hourDay}")
    List<Integer> selectActiveCouponIds(@Param("weekDay") int weekDay, @Param("hourDay") int hourDay);
}
