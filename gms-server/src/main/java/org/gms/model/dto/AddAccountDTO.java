package org.gms.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
public class AddAccountDTO implements Serializable {
    private String name;
    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
    private Integer language;
}
