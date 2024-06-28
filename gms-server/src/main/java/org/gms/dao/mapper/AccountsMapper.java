package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gms.dao.entity.AccountsDO;

/**
 *  映射层。
 *
 * @author sleep
 * @since 2024-05-24
 */
public interface AccountsMapper extends BaseMapper<AccountsDO> {
    @Update("UPDATE accounts SET loggedin = #{value}")
    void updateAllLoggedIn(Integer value);
    
    @Select("SELECT * FROM accounts WHERE name = #{name}")
    AccountsDO selectOneByName(String name);
    
    @Insert("INSERT INTO accounts(name, password, birthday, tempban, language) VALUES (#{name}, #{password}, #{birthday}, #{tempban}, #{language})")
    void addAccount(AccountsDO accountsDO);
}
