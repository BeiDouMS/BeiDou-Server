package dao.mapper;

import com.mybatisflex.core.BaseMapper;
import dao.entity.AccountsEntity;
import org.apache.ibatis.annotations.Select;

/**
 *  映射层。
 *
 * @author lee
 * @since 2024-07-03
 */
public interface AccountsMapper extends BaseMapper<AccountsEntity> {
    @Select("SELECT * FROM accounts WHERE name = #{name}")
    AccountsEntity selectOneByName(String name);
}
