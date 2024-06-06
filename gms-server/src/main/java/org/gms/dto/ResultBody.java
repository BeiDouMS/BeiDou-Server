package org.gms.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.gms.exception.BaseErrorInfoInterface;
import org.gms.exception.BizExceptionEnum;

import java.sql.Timestamp;

@Data
public class ResultBody {
    private Integer code;
    private String message;
    private Object data;
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public ResultBody() {
    }

    public ResultBody(BaseErrorInfoInterface errorInfo) {
        this.code = errorInfo.getResultCode();
        this.message = errorInfo.getResultMsg();
    }
    
    public static ResultBody success() {
        return success(null);
    }

    public static ResultBody success(Object data) {
        ResultBody rb = new ResultBody();
        rb.setCode(BizExceptionEnum.SUCCESS.getResultCode());
        rb.setMessage(BizExceptionEnum.SUCCESS.getResultMsg());
        rb.setData(data);
        return rb;
    }
    
    public static ResultBody error(BaseErrorInfoInterface errorInfo) {
        ResultBody rb = new ResultBody();
        rb.setCode(errorInfo.getResultCode());
        rb.setMessage(errorInfo.getResultMsg());
        rb.setData(null);
        return rb;
    }

    public static ResultBody error(Integer code, String message) {
        ResultBody rb = new ResultBody();
        rb.setCode(code);
        rb.setMessage(message);
        rb.setData(null);
        return rb;
    }

    public static ResultBody error(String message) {
        ResultBody rb = new ResultBody();
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
