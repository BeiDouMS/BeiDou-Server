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
 *  实体类。
 *
 * @author sleep
 * @since 2024-05-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("playernpcs_equip")
public class PlayernpcsEquipDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private Integer npcid;

    private Integer equipid;

    private Integer type;

    private Short equippos;

}
