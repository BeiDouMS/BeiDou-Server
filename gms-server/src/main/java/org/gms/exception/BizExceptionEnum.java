package org.gms.exception;

import org.gms.exception.BaseErrorInfoInterface;

public enum BizExceptionEnum implements BaseErrorInfoInterface {

    SUCCESS(20000, "成功!"),
    BODY_NOT_MATCH(40000, "请求的数据格式不符!"),
    REQUEST_METHOD_SUPPORT_ERROR(40001, "当前请求方法不支持"),
    NOT_FOUND(40004, "未找到该资源!"),
    INTERNAL_SERVER_ERROR(50000, "服务器内部错误!"),
    SERVER_BUSY(50003, "服务器正忙，请稍后再试!");

    private final Integer resultCode;
    private final String resultMsg;

    BizExceptionEnum(Integer resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @Override
    public Integer getResultCode() {
        return resultCode;
    }

    @Override
    public String getResultMsg() {
        return resultMsg;
    }
}
