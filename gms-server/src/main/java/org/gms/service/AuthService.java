package org.gms.service;

import org.gms.util.I18nUtil;
import org.gms.util.JwtUtils;
import org.gms.dao.entity.AccountsDO;
import org.gms.util.RequireUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        AccountsDO account = accountService.findByName(name);
        RequireUtil.requireFalse(account == null || !accountService.checkPassword(password, account),
                I18nUtil.getExceptionMessage("AuthService.account.or.password.error"));

        HashMap<String, String> result = new HashMap<>();
        result.put("token", jwtUtils.generateJwtToken(account.getName()));
        return result;
    }
}
