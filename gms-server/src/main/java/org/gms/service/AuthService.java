package org.gms.service;

import lombok.AllArgsConstructor;
import org.gms.util.I18nUtil;
import org.gms.util.JwtUtils;
import org.gms.dao.entity.AccountsDO;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthService {
    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    public Map<String, String> getToken(String name, String password) {
        AccountsDO account = accountService.findByName(name);
        RequireUtil.requireFalse(account == null || !accountService.checkPassword(password, account),
                I18nUtil.getExceptionMessage("AuthService.account.or.password.error"));

        HashMap<String, String> result = new HashMap<>();
        result.put("token", jwtUtils.generateJwtToken(account.getName()));
        return result;
    }

    public Map<String, String> refreshToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtils.getUserNameFromJwtToken(token);
            AccountsDO account = accountService.findByName(username);
            if (account == null) return null;
            HashMap<String, String> result = new HashMap<>();
            result.put("token", jwtUtils.generateJwtToken(account.getName()));
            return result;
        }
        return null;
    }
}
