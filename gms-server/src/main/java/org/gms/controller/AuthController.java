package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Tag(name = "/auth/" + ApiConstant.LATEST)
    @Operation(summary = "登录")
    @PostMapping("/" + ApiConstant.LATEST + "/login")
    public ResultBody<Map<String, String>> login(@RequestBody SubmitBody<Map<String, String>> data) {
        return ResultBody.success(authService.getToken(data.getData().get("username"), data.getData().get("password")));
    }

    @Tag(name = "/auth/" + ApiConstant.LATEST)
    @Operation(summary = "登出")
    @DeleteMapping("/" + ApiConstant.LATEST + "/logout")
    public ResultBody<Object> logout() {
        return ResultBody.success();
    }

    @Tag(name = "/auth/" + ApiConstant.LATEST)
    @Operation(summary = "刷新token")
    @GetMapping("/" + ApiConstant.LATEST + "/refreshToken")
    public ResultBody<Map<String, String>> refreshToken(@RequestHeader("Authorization") String token) {
        return ResultBody.success(authService.refreshToken(token));
    }
}
