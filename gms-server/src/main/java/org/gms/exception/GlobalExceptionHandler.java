package org.gms.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.gms.model.dto.ResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResultBody<Object> bizExceptionHandler(HttpServletRequest req, BizException e) {
        logger.error("发生业务异常！原因是：{}", e.getErrorMsg());
        return ResultBody.error(req, e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * IllegalArgumentException NullPointerException UnsupportedOperationException都是RuntimeException
     * 这里直接捕获RuntimeException来代替一个一个去捕获
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public ResultBody<Object> exceptionHandler(HttpServletRequest req, RuntimeException e) {
        logger.error("发生运行时异常！原因是:", e);
        return ResultBody.error(req, BizExceptionEnum.BODY_NOT_MATCH);
    }

    /**
     * 处理请求方法不支持的异常  
     */
    @ExceptionHandler(value = ServletException.class)
    @ResponseBody
    public ResultBody<Object> exceptionHandler(HttpServletRequest req, ServletException e) {
        logger.error("发生请求时异常！原因是:", e);
        return ResultBody.error(req, BizExceptionEnum.REQUEST_METHOD_SUPPORT);
    }
    /**
     * 处理其他异常  
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultBody<Object> exceptionHandler(HttpServletRequest req, Exception e) {
        logger.error("未知异常！原因是:", e);
        return ResultBody.error(req, BizExceptionEnum.INTERNAL_SERVER_ERROR);
    }
}
