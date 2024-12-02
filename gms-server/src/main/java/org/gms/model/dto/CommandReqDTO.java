package org.gms.model.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommandReqDTO extends BasePageDTO {

    private Integer id;

    private Integer level;//增改用的参数
    private List<Integer> levelList;//直接考虑多选条件构成传参


    private String syntax;//游戏中实际输入的指令

    private Integer defaultLevel;//增改用的参数
    private List<Integer> defaultLevelList;//查询用的参数

    private String clazz;
    private String description;//根据描述,模糊查询命令

    private Boolean enabled;//根据状态,精确查询命令信息

}
