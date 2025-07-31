package org.gms.constants.string;

import lombok.Getter;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 目前支持每日/每周，需要每月/季度/年度扩展时，请扩展此枚举
 */
@Getter
public enum ExtendType {
    ACCOUNT_EXTEND("11"),
    ACCOUNT_EXTEND_DAILY("12"),
    ACCOUNT_EXTEND_WEEKLY("13"),
    CHARACTER_EXTEND("21"),
    CHARACTER_EXTEND_DAILY("22"),
    CHARACTER_EXTEND_WEEKLY("23"),

    UNSUPPORTED("99");

    private final String type;

    ExtendType(String type) {
        this.type = type;
    }

    public static ExtendType getExtendType(String type) {
        for (ExtendType extendType : ExtendType.values()) {
            if (extendType.getType().equals(type)) {
                return extendType;
            }
        }
        return UNSUPPORTED;
    }

    public static Map<String, Date> getCleanMap() {
        Map<String, Date> map = new HashMap<>();
        Calendar mondayStart = Calendar.getInstance();
        mondayStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        mondayStart.set(Calendar.HOUR_OF_DAY, 0);
        mondayStart.set(Calendar.MINUTE, 0);
        mondayStart.set(Calendar.SECOND, 0);

        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);

        map.put(ACCOUNT_EXTEND_DAILY.getType(), new Date(todayStart.getTimeInMillis()));
        map.put(ACCOUNT_EXTEND_WEEKLY.getType(), new Date(mondayStart.getTimeInMillis()));
        map.put(CHARACTER_EXTEND_DAILY.getType(), new Date(todayStart.getTimeInMillis()));
        map.put(CHARACTER_EXTEND_WEEKLY.getType(), new Date(mondayStart.getTimeInMillis()));
        return map;
    }

    public static boolean isAccount(String type) {
        return ACCOUNT_EXTEND.getType().equals(type) || ACCOUNT_EXTEND_DAILY.getType().equals(type) || ACCOUNT_EXTEND_WEEKLY.getType().equals(type);
    }

     public static boolean isCharacter(String type) {
         return CHARACTER_EXTEND.getType().equals(type) || CHARACTER_EXTEND_DAILY.getType().equals(type) || CHARACTER_EXTEND_WEEKLY.getType().equals(type);
     }
}
