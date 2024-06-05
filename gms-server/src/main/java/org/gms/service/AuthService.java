package org.gms.service;

import org.gms.springboot.security.JwtUtils;
import org.gms.dao.entity.AccountsDO;
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
        if (account == null || !account.checkPassword(password)) return null;

        HashMap<String, String> result = new HashMap<>();
        result.put("token", jwtUtils.generateJwtToken(account.getName()));
        return result;
    }
}
