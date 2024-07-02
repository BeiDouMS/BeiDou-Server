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
@Table("makercreatedata")
public class MakercreatedataEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Id
    private Integer itemid;

    private Integer reqLevel;

    private Integer reqMakerLevel;

    private Integer reqMeso;

    private Integer reqItem;

    private Integer reqEquip;

    private Integer catalyst;

    private Integer quantity;

    private Integer tuc;

}
