package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 *  实体类。
 *
 * @author sleep
 * @since 2024-05-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("characters")
public class CharactersDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private Integer accountid;

    private Integer world;

    private String name;

    private Integer level;

    private Integer exp;

    private Integer gachaexp;

    private Integer str;

    private Integer dex;

    private Integer luk;

    private Integer int;

    private Integer hp;

    private Integer mp;

    private Integer maxhp;

    private Integer maxmp;

    private Integer meso;

    @Column("hpMpUsed")
    private Long hpMpUsed;

    private Integer job;

    private Integer skincolor;

    private Integer gender;

    private Integer fame;

    private Integer fquest;

    private Integer hair;

    private Integer face;

    private Integer ap;

    private String sp;

    private Integer map;

    private Integer spawnpoint;

    private Boolean gm;

    private Integer party;

    @Column("buddyCapacity")
    private Integer buddyCapacity;

    private Timestamp createdate;

    private Long rank;

    @Column("rankMove")
    private Integer rankMove;

    @Column("jobRank")
    private Long jobRank;

    @Column("jobRankMove")
    private Integer jobRankMove;

    private Long guildid;

    private Long guildrank;

    private Long messengerid;

    private Long messengerposition;

    private Integer mountlevel;

    private Integer mountexp;

    private Integer mounttiredness;

    private Integer omokwins;

    private Integer omoklosses;

    private Integer omokties;

    private Integer matchcardwins;

    private Integer matchcardlosses;

    private Integer matchcardties;

    private Integer merchantmesos;

    private Boolean hasmerchant;

    private Integer equipslots;

    private Integer useslots;

    private Integer setupslots;

    private Integer etcslots;

    @Column("familyId")
    private Integer familyId;

    private Integer monsterbookcover;

    @Column("allianceRank")
    private Integer allianceRank;

    @Column("vanquisherStage")
    private Long vanquisherStage;

    @Column("ariantPoints")
    private Long ariantPoints;

    @Column("dojoPoints")
    private Long dojoPoints;

    @Column("lastDojoStage")
    private Long lastDojoStage;

    @Column("finishedDojoTutorial")
    private Integer finishedDojoTutorial;

    @Column("vanquisherKills")
    private Long vanquisherKills;

    @Column("summonValue")
    private Long summonValue;

    @Column("partnerId")
    private Integer partnerId;

    @Column("marriageItemId")
    private Integer marriageItemId;

    private Integer reborns;

    private Integer pqpoints;

    @Column("dataString")
    private String dataString;

    @Column("lastLogoutTime")
    private Timestamp lastLogoutTime;

    @Column("lastExpGainTime")
    private Timestamp lastExpGainTime;

    @Column("partySearch")
    private Boolean partySearch;

    private Long jailexpire;

}
