package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.springboot.response.ResultBody;
import org.gms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Tag(name = "v1")
    @Operation(summary = "登录")
    @PostMapping("/v1/login")
    public ResultBody login(@RequestBody Map<String, String> data) {
        return ResultBody.success(authService.getToken(data.get("username"), data.get("password")));
    }

    @Tag(name = "v1")
    @Operation(summary = "登出")
    @DeleteMapping("/v1/logout")
    public ResultBody logout() {
        return ResultBody.success();
    }
}
