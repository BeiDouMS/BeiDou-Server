package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机结算请求。accountId 与 characterId 二选一：
 * 只传 accountId 时按该账号下所有角色逐个结算；传 characterId 时只结算单个角色。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5HangupReqDTO {
    private Integer accountId;
    private Integer characterId;
    /** 结算/切换使用的挂机地图，为空时沿用角色已保存的地图 */
    private Integer mapId;
    /** 掉落白名单物品列表，长度不设上限 */
    private List<Integer> itemIds;
}
