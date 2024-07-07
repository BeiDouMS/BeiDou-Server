package api.exception;

public enum ExceptionEnum implements BaseErrorInfoInterface {
    SUCCESS(20000, "成功!"),
    
    NOT_FOUND(40000, "未找到该资源!"),
    METHOD_ERROR(40001, "当前请求方法不支持"),
    BODY_NOT_MATCH(40002, "请求的数据格式不符!"),

    INTERNAL_SERVER_ERROR(50000, "服务器内部错误!"),
    SERVER_BUSY(50001, "服务器正忙，请稍后再试!"),
    REPEAT_SRC(50002, "数据已存在"),
    ILLEGAL_TOKEN(50003, "token非法!"),
    OTHER_CLIENTS_LOGGED_IN(50004, "token被其他客户端占用!"),
    TOKEN_EXPIRED(50005, "token已过期!"),
    
    ERROR_DATE_FORMAT(51000, "日期格式应为 yyyy-MM-dd"),
    ACCESS_DENIED(51001, "拒绝访问"),
    EXIST_QUEST_ID(51002, "任务ID已存在"),
    REPEAT_USERNAME(51003, "用户名已存在"),
    ACCOUNT_IS_ONLINE(51004, "该账号正在游戏中")
    ;

    private final int resultCode;
    private final String resultMsg;

    ExceptionEnum(int resultCode, String resultMsg) {
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
