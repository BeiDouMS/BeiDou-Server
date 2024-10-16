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

/**
 * 服务配置 实体类。
 *
 * @author CN
 * @since 2024-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("server_prop")
public class ServerPropDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 配置分类
     */
    private Integer propType;

    /**
     * 配置编码
     */
    private String propCode;

    /**
     * 配置值数据类型
     */
    private String propClass;

    /**
     * 配置值
     */
    private String propValue;

    /**
     * 配置描述
     */
    private String propDesc;

}
