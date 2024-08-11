package org.gms.constants.string;

import lombok.Getter;
import org.gms.util.I18nUtil;

@Getter
public enum CategoryType {
    MAIN(8, I18nUtil.getMessage("CategoryType.MAIN")),
    EVENT(1, I18nUtil.getMessage("CategoryType.EVENT")),
    EQUIP(2, I18nUtil.getMessage("CategoryType.EQUIP")),
    USE(3, I18nUtil.getMessage("CategoryType.USE")),
    SET(4, I18nUtil.getMessage("CategoryType.SET")),
    ETC(5, I18nUtil.getMessage("CategoryType.ETC")),
    PET(6, I18nUtil.getMessage("CategoryType.PET")),
    PACKAGE(7, I18nUtil.getMessage("CategoryType.PACKAGE")),
    ;

    private final int id;
    private final String name;

    CategoryType(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static CategoryType ofId(int id) {
        for (CategoryType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static String toName(int id) {
        CategoryType categoryType = ofId(id);
        return categoryType == null ? "" : categoryType.getName();
    }
}
