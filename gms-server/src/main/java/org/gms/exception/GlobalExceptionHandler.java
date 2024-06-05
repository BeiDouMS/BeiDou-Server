package org.gms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.gms.dto.ResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常  
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public ResultBody bizExceptionHandler(HttpServletRequest req, BizException e) {
        logger.error("发生业务异常！原因是：{}", e.getErrorMsg());
        return ResultBody.error(e.getErrorCode(), e.getErrorMsg());
    }


    /**
     * 处理空指针的异常  
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e) {
        logger.error("发生空指针异常！原因是:", e);
        return ResultBody.error(BizExceptionEnum.BODY_NOT_MATCH);
    }

    /**
     * 处理请求方法不支持的异常  
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e) {
        logger.error("发生请求方法不支持异常！原因是:", e);
        return ResultBody.error(BizExceptionEnum.REQUEST_METHOD_SUPPORT_ERROR);
    }
    /**
     * 处理其他异常  
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, Exception e) {
        logger.error("未知异常！原因是:", e);
        return ResultBody.error(BizExceptionEnum.INTERNAL_SERVER_ERROR);
    }
}
