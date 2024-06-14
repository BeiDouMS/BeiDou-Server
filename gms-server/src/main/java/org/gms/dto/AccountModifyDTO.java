package org.gms.dto;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gms.dao.entity.AccountsDO;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("accounts")
public class AccountModifyDTO extends AccountsDO implements Serializable {
    private String modifyType;
}
