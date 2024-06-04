package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.gms.dao.entity.NxcodeItemsDO;

/**
 *  映射层。
 *
 * @author sleep
 * @since 2024-05-24
 */
public interface NxcodeItemsMapper extends BaseMapper<NxcodeItemsDO> {
    @Delete("DELETE FROM nxcode_items WHERE codeid IN (SELECT id FROM nxcode WHERE expiration <= #{timeClear})")
    void clearExpirations(long timeClear);
}
