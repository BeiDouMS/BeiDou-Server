package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.gms.dao.entity.CharactersDO;

/**
 *  映射层。
 *
 * @author sleep
 * @since 2024-05-24
 */
public interface CharactersMapper extends BaseMapper<CharactersDO> {
    @Update("UPDATE characters SET HasMerchant = #{value}")
    void updateAllHasMerchant(Integer value);
}
