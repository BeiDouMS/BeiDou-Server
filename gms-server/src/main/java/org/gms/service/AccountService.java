package org.gms.service;

import org.gms.dao.entity.AccountsDO;
import org.gms.dao.mapper.AccountsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountsMapper accountsMapper;

    @Autowired
    public AccountService(AccountsMapper accountsMapper) {
        this.accountsMapper = accountsMapper;
    }

    public AccountsDO findByName(String name) {
        return accountsMapper.selectOneByName(name);
    }

    public AccountsDO getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findByName(userDetails.getUsername());
    }
}
