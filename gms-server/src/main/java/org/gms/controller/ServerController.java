package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.constants.api.ApiConstant;
import org.gms.dto.ChannelListRtnDTO;
import org.gms.net.server.Server;
import org.gms.dto.ResultBody;
import org.gms.service.ServerService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/server")
public class ServerController {
    private final ApplicationContext applicationContext;
    private final ServerService serverService;

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "停止所有")
    @GetMapping("/" + ApiConstant.LATEST + "/stop")
    public void stop() {
        // 这里只能触发destroy，但服务不能正常停止
        SpringApplication.exit(applicationContext);
        // 这里才能正常的停止
        System.exit(0);
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "停止服务")
    @GetMapping("/" + ApiConstant.LATEST + "/stopServer")
    public ResultBody<Object> stopServer() {
        Server.getInstance().shutdownInternal(false);
        return ResultBody.success();
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "启动服务")
    @GetMapping("/" + ApiConstant.LATEST + "/startServer")
    public ResultBody<Object> startServer() {
        Server.getInstance().init();
        return ResultBody.success();
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "重启服务")
    @GetMapping("/" + ApiConstant.LATEST + "/restartServer")
    public ResultBody<Object> restartServer() {
        Server.getInstance().shutdownInternal(true);
        return ResultBody.success();
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "查询服务状态")
    @GetMapping("/" + ApiConstant.LATEST + "/online")
    public ResultBody<Boolean> online() {
        return ResultBody.success(Server.getInstance().isOnline());
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "大区列表")
    @GetMapping("/" + ApiConstant.LATEST + "/world/list")
    public ResultBody<Object> worldList() {
        return ResultBody.success(serverService.worldList());
    }

    @Tag(name = "/server/" + ApiConstant.LATEST)
    @Operation(summary = "频道列表")
    @GetMapping("/" + ApiConstant.LATEST + "/channel/list")
    public ResultBody<List<ChannelListRtnDTO>> channelList(@RequestParam int worldId) {
        return ResultBody.success(serverService.channelList(worldId));
    }
}
