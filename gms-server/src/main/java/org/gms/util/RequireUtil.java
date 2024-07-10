package org.gms.util;

import org.gms.exception.BizException;
import org.gms.exception.BizExceptionEnum;

import java.util.Map;
import java.util.function.BiConsumer;

public class RequireUtil {
    public static void requireNull(Object obj) {
        requireNull(obj, null);
    }

    public static void requireNull(Object obj, String msg) {
        if (obj == null) {
            return;
        }
        // 有无错误信息
        if (msg == null) {
            throw new IllegalArgumentException();
        } else {
            throw new BizException(BizExceptionEnum.BODY_NOT_MATCH.getResultCode(), msg);
        }
    }

    public static void requireNotNull(Object obj) {
        requireNull(obj, null);
    }

    public static void requireNotNull(Object obj, String msg) {
        if (obj != null) {
            return;
        }
        // 有无错误信息
        if (msg == null) {
            throw new IllegalArgumentException();
        } else {
            throw new BizException(BizExceptionEnum.BODY_NOT_MATCH.getResultCode(), msg);
        }
    }

    public static void requireNotEmpty(Object obj) {
        requireNotEmpty(obj, null);
    }

    public static void requireNotEmpty(Object obj, String msg) {
        if (!isEmpty(obj)) {
            return;
        }

        // 有无错误信息
        if (msg == null) {
            throw new IllegalArgumentException();
        } else {
            throw new BizException(BizExceptionEnum.BODY_NOT_MATCH.getResultCode(), msg);
        }
    }

    public static void requireTrue(boolean b, String msg) {
        if (!b) throw new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS.getResultCode(), msg);
    }

    public static void requireFalse(boolean b, String msg) {
        if (b) throw new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS.getResultCode(), msg);
    }

    public static boolean isEmpty(Object obj) {
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
        return empty;
    }

    public static void requireNotEmptyOrElse(Object obj, Runnable runnable) {
        if (!isEmpty(obj)) {
            return;
        }
        runnable.run();
    }

    public static void requireNotEmptyAndThen(Object obj, Runnable runnable) {
        if (isEmpty(obj)) {
            return;
        }
        runnable.run();
    }

    public static <T, R> void requireNotEmptyAndThen(T t, R r, BiConsumer<T, R> consumer) {
        if (isEmpty(t) || isEmpty(r)) {
            return;
        }
        consumer.accept(t, r);
    }
}
