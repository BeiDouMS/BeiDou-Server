package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.dto.ResultBody;
import org.gms.dto.SubmitBody;
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
    public ResultBody login(@RequestBody SubmitBody<Map<String, String>> data) {
        return ResultBody.success(authService.getToken(data.getData().get("username"), data.getData().get("password")));
    }

    @Tag(name = "/auth/" + ApiConstant.LATEST)
    @Operation(summary = "登出")
    @DeleteMapping("/" + ApiConstant.LATEST + "/logout")
    public ResultBody logout() {
        return ResultBody.success();
    }
}