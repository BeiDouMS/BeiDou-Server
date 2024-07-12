package org.gms.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    
    protected Integer errorCode;
    protected String errorMsg;

    public BizException() {
        super();
    }

    public BizException(BaseErrorInfoInterface errorInfoInterface) {
        super(String.valueOf(errorInfoInterface.getResultCode()));
        this.errorCode = errorInfoInterface.getResultCode();
        this.errorMsg = errorInfoInterface.getResultMsg();
    }

    public BizException(BaseErrorInfoInterface errorInfoInterface, Throwable cause) {
        super(String.valueOf(errorInfoInterface.getResultCode()), cause);
        this.errorCode = errorInfoInterface.getResultCode();
        this.errorMsg = errorInfoInterface.getResultMsg();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg) {
        super(String.valueOf(errorCode));
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg, Throwable cause) {
        super(String.valueOf(errorCode), cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static BizException illegalArgument() {
        return new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS);
    }

    public static BizException illegalArgument(String errorMsg) {
        return new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS.getResultCode(), errorMsg);
    }

    public static void throwIllegalArgument() {
        // 在这里throw堆栈会多一层
        throw new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS);
    }

    public static void throwIllegalArgument(String errorMsg) {
        // 在这里throw堆栈会多一层
        throw new BizException(BizExceptionEnum.ILLEGAL_PARAMETERS.getResultCode(), errorMsg);
    }

    public String getMessage() {
        return errorMsg;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
