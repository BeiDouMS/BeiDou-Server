package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;

/**
 * 扩展字段表 实体类。
 *
 * @author CN
 * @since 2024-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("extend_value")
public class ExtendValueDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 扩展字段id
     */
    @Id
    private String extendId;

    /**
     * 扩展字段类型，11-账号，12-账号日清，13-账号周清；21-角色，22-角色日清，23-角色周清
     */
    @Id
    private String extendType;

    /**
     * 扩展字段名称
     */
    @Id
    private String extendName;

    /**
     * 扩展字段值
     */
    private String extendValue;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
