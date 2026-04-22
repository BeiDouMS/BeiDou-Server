package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.util.Date;

/**
 * 自动封禁配置表 实体类。
 *
 * @author Nap
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("autoban_config")
public class AutobanConfigDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 封禁类型，对应AutobanFactory枚举名
     */
    private String type;

    /**
     * 是否禁用，true=禁用该类型封禁检测
     */
    private Boolean disabled;

    /**
     * 触发封禁所需积分，NULL使用枚举默认值
     */
    private Integer points;

    /**
     * 积分过期时间(毫秒)，NULL使用枚举默认值
     */
    private Long expireTime;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
