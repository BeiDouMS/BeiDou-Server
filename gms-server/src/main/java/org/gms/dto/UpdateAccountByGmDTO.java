package org.gms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gms.dao.entity.AccountsDO;

import java.io.Serializable;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class UpdateAccountByGmDTO implements Serializable {
    private String newPwd;
    private String pin;
    private String pic;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    @Column("nxCredit")
    private Integer nxCredit;
    @Column("maplePoint")
    private Integer maplePoint;
    @Column("nxPrepaid")
    private Integer nxPrepaid;
    private Integer characterslots;
    private Integer gender;
    private Integer webadmin;
    private String nick;
    private Integer mute;
    private String email;
    private Integer rewardpoints;
    private Integer votepoints;
    private Integer language;
}
