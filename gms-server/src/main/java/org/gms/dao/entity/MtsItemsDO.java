package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
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
@Table("mts_items")
public class MtsItemsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer tab;

    private Integer type;

    private Long itemid;

    private Integer quantity;

    private Integer seller;

    private Integer price;

    private Integer bidIncre;

    private Integer buyNow;

    private Integer position;

    private Integer upgradeslots;

    private Integer level;

    private Integer itemlevel;

    private Long itemexp;

    private Integer ringid;

    private Integer str;

    private Integer dex;

    @Column("int")
    private Integer inte;

    private Integer luk;

    private Integer hp;

    private Integer mp;

    private Integer watk;

    private Integer matk;

    private Integer wdef;

    private Integer mdef;

    private Integer acc;

    private Integer avoid;

    private Integer hands;

    private Integer speed;

    private Integer jump;

    private Integer locked;

    private Integer isequip;

    private String owner;

    private String sellername;

    private String sellEnds;

    private Integer transfer;

    private Long vicious;

    private Long flag;

    private Long expiration;

    @Column("giftFrom")
    private String giftFrom;

}
