package org.gms.model.dto;

import lombok.*;

import java.sql.Timestamp;

/**
 * 账号下角色列表项 DTO（GM后台展示用）。
 *
 * @author beidou
 * @since 2026-07-09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CharacterListItemDTO {
    private Integer id;
    private String name;
    private Integer job;
    private String jobName;
    private Integer level;
    private Integer world;
    private String worldName;
    private Integer gm;
    private Integer meso;
    private Integer fame;
    private Integer guildid;
    private Timestamp createdate;
    private Timestamp lastLogoutTime;
    private boolean online;
}
