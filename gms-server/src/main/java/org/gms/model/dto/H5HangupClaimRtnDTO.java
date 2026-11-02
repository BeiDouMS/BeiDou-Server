package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机收益领取结果。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5HangupClaimRtnDTO {
    private Integer characterId;
    /** 角色是否在线；离线时收益保留，登录端游后自动发放 */
    private Boolean online;
    private Long givenMeso;
    private Long givenExp;
    private Long pendingMeso;
    private Long pendingExp;
    /** 本次实际发放的物品 */
    private List<H5ItemRtnDTO> givenItems;
    /** 背包放不下而保留的物品 */
    private List<H5ItemRtnDTO> pendingItems;
}
