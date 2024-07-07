package service;

import api.exception.ExceptionEnum;
import dao.entity.AccountsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.JwtUtils;
import utils.RequireUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(AccountService accountService, JwtUtils jwtUtils) {
        this.accountService = accountService;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, String> getToken(String name, String password) {
        AccountsEntity account = accountService.findByName(name);
        RequireUtil.requireFalse(account == null || !accountService.checkPassword(password, account), ExceptionEnum.NOT_FOUND, "账号或密码错误");

        HashMap<String, String> result = new HashMap<>();
        result.put("token", jwtUtils.generateJwtToken(account.getName()));
        return result;
    }
}
