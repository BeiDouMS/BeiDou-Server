package api.controller;

import lombok.AllArgsConstructor;
import model.ResultBody;
import net.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/server")
public class ServerController {
    private final ApplicationContext applicationContext;

    @GetMapping("/stop")
    @PreAuthorize("hasRole('ADMIN')")
    public void stop() {
        // 这里只能触发destroy，但服务不能正常停止
        SpringApplication.exit(applicationContext);
        // 这里才能正常的停止
        System.exit(0);
    }

    @GetMapping("/stopServer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> stopServer() {
        Server.getInstance().shutdownInternal(false);
        return ResultBody.success();
    }

    @GetMapping("/startServer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> startServer() {
        Server.getInstance().init();
        return ResultBody.success();
    }

    @GetMapping("/restartServer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> restartServer() {
        Server.getInstance().shutdownInternal(true);
        return ResultBody.success();
    }

    @GetMapping("/online")
    public ResultBody<Boolean> online() {
        return ResultBody.success(Server.getInstance().isOnline());
    }
}
