package dao.mapper;

import com.mybatisflex.core.BaseMapper;
import dao.entity.CharactersEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *  映射层。
 *
 * @author lee
 * @since 2024-07-03
 */
public interface CharactersMapper extends BaseMapper<CharactersEntity> {
    @Select("SELECT id, world FROM characters WHERE accountid = #{accountId}")
    List<CharactersEntity> selectIdAndWorldListByAccountId(int accountId);
}
