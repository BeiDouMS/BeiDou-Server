package api.controller;

import model.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResultBody<Map<String, String>> login(@RequestBody Map<String, String> data) {
        return ResultBody.success(authService.getToken(data.get("username"), data.get("password")));
    }

    @DeleteMapping("/logout")
    public ResultBody<Object> logout() {
        return ResultBody.success();
    }
}
