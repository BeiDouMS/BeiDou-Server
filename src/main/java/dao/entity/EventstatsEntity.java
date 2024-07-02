package dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 *  实体类。
 *
 * @author lee
 * @since 2024-07-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("eventstats")
public class EventstatsEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long characterid;

    /**
     * 0
     */
    private String name;

    private Integer info;

}
