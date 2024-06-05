package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gms.net.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/server")
public class ServerController {
    private final ApplicationContext applicationContext;

    @Tag(name = "v1")
    @Operation(summary = "停止所有")
    @GetMapping("/v1/stop")
    public void stop() {
        SpringApplication.exit(applicationContext);
    }

    @Tag(name = "v1")
    @Operation(summary = "停止服务")
    @GetMapping("/v1/stopServer")
    public void stopServer() {
        Server.getInstance().shutdownInternal(false);
    }

    @Tag(name = "v1")
    @Operation(summary = "启动服务")
    @GetMapping("/v1/startServer")
    public void startServer() {
        Server.getInstance().init();
    }

    @Tag(name = "v1")
    @Operation(summary = "重启服务")
    @GetMapping("/v1/restartServer")
    public void restartServer() {
        Server.getInstance().shutdownInternal(true);
    }

    @Tag(name = "v1")
    @Operation(summary = "查询服务状态")
    @GetMapping("/v1/online")
    public boolean online() {
        return Server.getInstance().isOnline();
    }
}
