package utils;


import api.exception.BizException;
import api.exception.ExceptionEnum;

import java.util.Map;

public class RequireUtil {
    public static void requireNull(Object obj, ExceptionEnum exceptionEnum) {
        requireNull(obj, exceptionEnum, exceptionEnum.getResultMsg());
    }
    
    public static void requireNull(Object obj, ExceptionEnum exceptionEnum, String msg) {
        if (obj == null) {
            return;
        }
        throw new BizException(exceptionEnum.getResultCode(), msg);
    }

    public static void requireNotNull(Object obj, ExceptionEnum exceptionEnum, String msg) {
        if (obj != null) {
            return;
        }
        throw new BizException(exceptionEnum.getResultCode(), msg);
    }

    public static void requireNotEmpty(Object obj, ExceptionEnum exceptionEnum, String msg) {
        boolean empty = false;
        switch (obj) {
            case null -> empty = true;
            case String str -> {
                if (str.trim().isEmpty()) {
                    empty = true;
                }
            }
            case Iterable<?> iterable -> {
                if (!iterable.iterator().hasNext()) {
                    empty = true;
                }
            }
            case Object[] array -> {
                if (array.length == 0) {
                    empty = true;
                }
            }
            case Map<?, ?> map -> {
                if (map.isEmpty()) {
                    empty = true;
                }
            }
            default -> {
            }
        }
        if (!empty) {
            return;
        }

        throw new BizException(exceptionEnum.getResultCode(), msg);
    }

    public static void requireTrue(boolean b, ExceptionEnum exceptionEnum, String msg) {
        if (!b) throw new BizException(exceptionEnum.getResultCode(), msg);
    }

    public static void requireFalse(boolean b, ExceptionEnum exceptionEnum) {
        requireFalse(b, exceptionEnum, exceptionEnum.getResultMsg());
    }
    
    public static void requireFalse(boolean b, ExceptionEnum exceptionEnum, String msg) {
        if (b) throw new BizException(exceptionEnum.getResultCode(), msg);
    }
}
