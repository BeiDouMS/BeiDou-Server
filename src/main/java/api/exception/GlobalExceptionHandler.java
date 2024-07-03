package api.exception;

import model.ResultBody;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public ResultBody bizExceptionHandler(HttpServletRequest req, BizException e) {
        log.error("业务异常！错误信息：{}", e.getErrorMsg());
        return ResultBody.error(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * 处理空指针的异常
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e) {
        log.error("空指针异常！错误信息：", e);
        return ResultBody.error(ExceptionEnum.BODY_NOT_MATCH);
    }

    /**
     * 处理请求方法不支持的异常
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持！错误信息：", e);
        return ResultBody.error(ExceptionEnum.METHOD_ERROR);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, Exception e) {
        log.error("未知错误！错误信息:", e);
        return ResultBody.error(ExceptionEnum.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, AccessDeniedException e) {
        log.error("Token 没有指定 API 的访问权限：", e);
        return ResultBody.error(ExceptionEnum.ACCESS_DENIED);
    }
}
