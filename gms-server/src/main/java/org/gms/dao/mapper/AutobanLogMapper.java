package org.gms.dao.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.gms.dao.entity.AutobanLogDO;
import org.gms.model.dto.AutobanLogSummaryReqDTO;
import org.gms.model.dto.AutobanLogSummaryRtnDTO;

import java.sql.Timestamp;
import java.util.List;

/**
 * 反作弊触发事件流水 Mapper。
 *
 * @author Nap
 * @since 2026-09-12
 */
public interface AutobanLogMapper extends BaseMapper<AutobanLogDO> {

    /**
     * 按角色汇总触发次数（问题角色 TOP）。
     */
    @Select("""
            <script>
            SELECT character_id AS characterId,
                   character_name AS characterName,
                   account_id AS accountId,
                   account_name AS accountName,
                   COUNT(*) AS total,
                   SUM(action = 'ALERT') AS alertCount,
                   SUM(action = 'POINT') AS pointCount,
                   SUM(action = 'AUTOBAN') AS autobanCount,
                   SUM(action = 'DISCONNECT') AS disconnectCount,
                   GROUP_CONCAT(DISTINCT type ORDER BY type SEPARATOR ',') AS types,
                   MAX(create_time) AS lastTime,
                   SUBSTRING_INDEX(GROUP_CONCAT(ip ORDER BY create_time DESC, id DESC SEPARATOR ','), ',', 1) AS lastIp
            FROM autoban_log
            <where>
                <if test="req.startTime != null">AND create_time &gt;= #{req.startTime}</if>
                <if test="req.endTime != null">AND create_time &lt;= #{req.endTime}</if>
                <if test="req.action != null and req.action != ''">AND action = #{req.action}</if>
            </where>
            GROUP BY character_id, character_name, account_id, account_name
            ORDER BY total DESC, lastTime DESC
            LIMIT #{req.limit}
            </script>
            """)
    List<AutobanLogSummaryRtnDTO> selectSummary(@Param("req") AutobanLogSummaryReqDTO req);

    /**
     * 分批删除指定时间之前的流水。
     *
     * @return 本批删除行数
     */
    @Delete("DELETE FROM autoban_log WHERE create_time < #{createTime} LIMIT #{limit}")
    int deleteBefore(@Param("createTime") Timestamp createTime, @Param("limit") int limit);
}
