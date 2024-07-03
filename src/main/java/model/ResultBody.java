package model;

import com.alibaba.fastjson.JSONObject;
import api.exception.BaseErrorInfoInterface;
import api.exception.ExceptionEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ResultBody {
    private int code;
    private String message;
    private Object data;
    @Setter(AccessLevel.NONE)
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
        rb.setCode(ExceptionEnum.SUCCESS.getResultCode());
        rb.setMessage(ExceptionEnum.SUCCESS.getResultMsg());
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
    
    public static ResultBody error(int code, String msg) {
        ResultBody rb = new ResultBody();
        rb.setCode(code);
        rb.setMessage(msg);
        rb.setData(null);
        return rb;
    }

    public static ResultBody error(String msg) {
        ResultBody rb = new ResultBody();
        rb.setCode(-1);
        rb.setMessage(msg);
        rb.setData(null);
        return rb;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
