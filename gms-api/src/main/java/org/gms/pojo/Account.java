package org.gms.pojo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

/**
 * 比如有多数据源
 * 驼峰不转下划线
 */
@Table(value = "accounts", dataSource = "mysql", camelToUnderline = false)
@Data
public class Account {
    @Id(keyType = KeyType.Auto)
    private Integer id;
    private String name;
    private String password;
    private String pin;
    private String pic;
    @Column(value = "loggedin")
    private boolean loggedIn;
    @Column(value = "lastlogin")
    private Date lastLogin;
    @Column(value = "createdat")
    private Date createDate;
    private Date birthday;
    private boolean banned;
    @Column(value = "banreason")
    private String banReason;
    private String macs;
    private Integer nxCredit;
    private Integer maplePoint;
    private Integer nxPrepaid;
    @Column(value = "characterslots")
    private Integer characterSlots;
    private Integer gender;
    @Column(value = "tempban")
    private Date tempBan;
    @Column(value = "greason")
    private Integer gReason;
    private boolean tos;
    @Column(value = "sitelogged")
    private String siteLogged;
    @Column(value = "webadmin")
    private Integer webAdmin;
    private String nick;
    private Integer mute;
    private String email;
    private String ip;
    @Column(value = "rewardpoints")
    private Integer rewardPoints;
    @Column(value = "votepoints")
    private Integer votePoints;
    @Column(value = "hwid")
    private String hwId;
    private Integer language;
}
