package org.gms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关服请求参数")
public class ServerShutdownDTO {

    @Schema(name = "minutes", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "多少分钟关闭，如果输入小于等于0的数字，则会默认1分钟关闭")
    private int minutes;

    @Schema(name = "shutdownMsg", example = "服务器将马上停止，请抓紧时间下线",
            requiredMode = Schema.RequiredMode.AUTO,
            description = "关服消息，如果不输入或者输入null，则会使用系统默认的停服通知消息")
    private String shutdownMsg;

    @Schema(name = "showServerMsg", example = "true",
            requiredMode = Schema.RequiredMode.AUTO,
            description = "是否使用顶部黄色服务器通知，服务重启之后可以重置提示消息。")
    private Boolean showServerMsg = false;

    @Schema(name = "showCenterMsg", example = "true",
            requiredMode = Schema.RequiredMode.AUTO,
            description = "是否使用中央屏幕提示消息。")
    private Boolean showCenterMsg = false;

    @Schema(name = "showChatMsg", example = "true",
            requiredMode = Schema.RequiredMode.AUTO,
            description = "是否使用聊天框给玩家发送蓝色GM消息")
    private Boolean showChatMsg = false;

}
