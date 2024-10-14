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
@Table("playernpcs")
public class PlayernpcsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    private Integer hair;

    private Integer face;

    private Integer skin;

    private Integer gender;

    private Integer x;

    private Integer cy;

    private Integer world;

    private Integer map;

    private Integer dir;

    private Integer scriptid;

    private Integer fh;

    private Integer rx0;

    private Integer rx1;

    private Integer worldrank;

    private Integer overallrank;

    private Integer worldjobrank;

    private Integer job;

}
