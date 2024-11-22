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
 * 游戏参数表 实体类。
 *
 * @author CN
 * @since 2024-10-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("game_config")
public class GameConfigDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 参数类型
     */
    private String configType;

    /**
     * 参数子类型
     */
    private String configSubType;

    /**
     * 参数值java类型
     */
    private String configClazz;

    /**
     * 参数名
     */
    private String configCode;

    /**
     * 参数值
     */
    private String configValue;

    /**
     * 参数描述
     */
    private String configDesc;

    /**
     * 更新时间
     */
    private Date updateTime;
}
