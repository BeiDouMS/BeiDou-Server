package org.gms.dto;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gms.dao.entity.AccountsDO;

import java.io.Serializable;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class AccountsSearchDTO extends AccountsDO implements Serializable {
    @Column("lastlogin")
    private Timestamp lastloginStart;
    @Column("lastlogin")
    private Timestamp lastloginEnd;
    @Column("createdat")
    private Timestamp createdatStart;
    @Column("createdat")
    private Timestamp createdatEnd;
}
