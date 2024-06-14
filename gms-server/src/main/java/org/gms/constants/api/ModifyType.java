package org.gms.constants.api;

import lombok.Getter;

@Getter
public enum ModifyType {
    INSERT("insert", "新增"),
    UPDATE("update", "更新"),
    DELETE("delete", "删除"),

    UNKNOWN("unknown", "未知");
    ;
    private final String code;
    private final String desc;

    ModifyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ModifyType getByCode(String code) {
        for (ModifyType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
