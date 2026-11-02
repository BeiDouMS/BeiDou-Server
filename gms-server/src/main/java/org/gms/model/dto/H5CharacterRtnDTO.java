package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H5 挂机角色战斗力信息。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5CharacterRtnDTO {
    private Integer characterId;
    private String characterName;
    private Integer level;
    private Integer job;
    /** physical / magic */
    private String attackType;
    private Integer str;
    private Integer dex;
    private Integer intValue;
    private Integer luk;
    private Integer watk;
    private Integer matk;
    /** 每秒伤害 */
    private Double dps;
    private Integer mapId;
    private String mapName;
}
