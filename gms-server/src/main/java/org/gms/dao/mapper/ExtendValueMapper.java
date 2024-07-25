package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.gms.dao.entity.ExtendValueDO;

import java.sql.Date;
import java.util.List;

/**
 * 扩展字段表 映射层。
 *
 * @author CN
 * @since 2024-07-08
 */
public interface ExtendValueMapper extends BaseMapper<ExtendValueDO> {
    @Delete("delete from extend_value where extend_type = #{extendType} and create_time < #{createTime}")
    void clean(@Param("extendType") String extendType, @Param("createTime") Date createTime);
}
