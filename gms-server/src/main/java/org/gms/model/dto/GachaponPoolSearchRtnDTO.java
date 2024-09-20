package org.gms.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gms.dao.entity.GachaponRewardPoolDO;

@EqualsAndHashCode(callSuper = true)
@Data
public class GachaponPoolSearchRtnDTO extends GachaponRewardPoolDO {
    private Integer realProb;
}
