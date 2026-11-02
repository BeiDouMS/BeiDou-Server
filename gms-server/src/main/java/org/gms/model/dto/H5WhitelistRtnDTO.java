package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机掉落白名单。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5WhitelistRtnDTO {
    private Integer characterId;
    private Integer mapId;
    /** 当前地图可掉落物品及勾选状态 */
    private List<H5DropRtnDTO> items;
    /** 角色已启用的全部拾取物品（含其他地图） */
    private List<H5DropRtnDTO> whitelistedItems;
}
