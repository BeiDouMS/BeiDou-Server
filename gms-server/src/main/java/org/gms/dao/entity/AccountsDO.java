package org.gms.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

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
@Table("accounts")
public class AccountsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    @JsonIgnore
    private String password;

    private String pin;

    private String pic;

    private Integer loggedin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastlogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private Boolean banned;

    private String banreason;

    private String macs;

    @Column("nxCredit")
    private Integer nxCredit;

    @Column("maplePoint")
    private Integer maplePoint;

    @Column("nxPrepaid")
    private Integer nxPrepaid;

    private Integer characterslots;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp tempban;

    private Integer greason;

    private Boolean tos;

    private String sitelogged;

    private Integer webadmin;

    private String nick;

    private Integer mute;

    private String email;

    private String ip;

    private Integer rewardpoints;

    private Integer votepoints;

    private String hwid;

    private Integer language;
}
