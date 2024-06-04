package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.gms.dao.entity.NxcodeDO;

/**
 *  映射层。
 *
 * @author sleep
 * @since 2024-05-24
 */
public interface NxcodeMapper extends BaseMapper<NxcodeDO> {
    @Delete("DELETE FROM nxcode WHERE expiration <= #{timeClear}")
    void clearExpirations(long timeClear);
}
