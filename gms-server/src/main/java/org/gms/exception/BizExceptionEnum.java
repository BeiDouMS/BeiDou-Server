package org.gms.exception;

import org.gms.util.I18nUtil;

public enum BizExceptionEnum implements BaseErrorInfoInterface {

    SUCCESS(20000, I18nUtil.getExceptionMessage("SUCCESS")),
    BODY_NOT_MATCH(40000, I18nUtil.getExceptionMessage("BODY_NOT_MATCH")),
    REQUEST_METHOD_SUPPORT(40001, I18nUtil.getExceptionMessage("REQUEST_METHOD_SUPPORT")),
    ILLEGAL_PARAMETERS(40002, I18nUtil.getExceptionMessage("ILLEGAL_PARAMETERS")),
    NOT_FOUND(40004, I18nUtil.getExceptionMessage("NOT_FOUND")),
    INTERNAL_SERVER_ERROR(50000, I18nUtil.getExceptionMessage("INTERNAL_SERVER_ERROR")),
    SERVER_BUSY(50003, I18nUtil.getExceptionMessage("SERVER_BUSY"));

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
