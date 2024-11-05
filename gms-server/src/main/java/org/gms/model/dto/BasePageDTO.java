package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 仅针对入参，出参统一用mybatis-flex的Page
 * @see com.mybatisflex.core.paginate.Page
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageDTO {
    /**
     * 页码
     */
    private Integer pageNo;
    /**
     * 每页条数
     */
    private Integer pageSize;
    /**
     * 只统计总数
     */
    private boolean onlyTotal;
    /**
     * 不分页
     */
    private boolean notPage;
}
