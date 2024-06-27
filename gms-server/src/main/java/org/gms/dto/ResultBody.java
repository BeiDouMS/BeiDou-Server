package org.gms.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.gms.exception.BaseErrorInfoInterface;
import org.gms.exception.BizExceptionEnum;

import java.sql.Timestamp;

@Data
public class ResultBody<T> {
    private Integer code;
    private String message;
    private T data;
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public ResultBody() {
    }

    public ResultBody(BaseErrorInfoInterface errorInfo) {
        this.code = errorInfo.getResultCode();
        this.message = errorInfo.getResultMsg();
    }

    public static <T> ResultBody<T> success() {
        return success(null);
    }

    public static <T> ResultBody<T> success(T data) {
        ResultBody<T> rb = new ResultBody<>();
        rb.setCode(BizExceptionEnum.SUCCESS.getResultCode());
        rb.setMessage(BizExceptionEnum.SUCCESS.getResultMsg());
        rb.setData(data);
        return rb;
    }

    public static <T> ResultBody<T> error(BaseErrorInfoInterface errorInfo) {
        ResultBody<T> rb = new ResultBody<>();
        rb.setCode(errorInfo.getResultCode());
        rb.setMessage(errorInfo.getResultMsg());
        rb.setData(null);
        return rb;
    }

    public static <T> ResultBody<T> error(Integer code, String message) {
        ResultBody<T> rb = new ResultBody<>();
        rb.setCode(code);
        rb.setMessage(message);
        rb.setData(null);
        return rb;
    }

    public static <T> ResultBody<T> error(String message) {
        ResultBody<T> rb = new ResultBody<>();
        rb.setCode(-1);
        rb.setMessage(message);
        rb.setData(null);
        return rb;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
