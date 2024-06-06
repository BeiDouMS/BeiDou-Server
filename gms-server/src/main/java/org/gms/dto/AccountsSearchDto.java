package org.gms.dto;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gms.dao.entity.AccountsDO;

import java.sql.Timestamp;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class AccountsSearchDto extends AccountsDO {
    @Column("lastlogin")
    private Timestamp lastloginStart;
    @Column("lastlogin")
    private Timestamp lastloginEnd;
    @Column("createdat")
    private Timestamp createdatStart;
    @Column("createdat")
    private Timestamp createdatEnd;
}
