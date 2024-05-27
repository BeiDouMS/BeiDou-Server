package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Tag(name = "v1")
    @Operation(summary = "登录")
    @PostMapping("/v1/login")
    public void login() {

    }
}
