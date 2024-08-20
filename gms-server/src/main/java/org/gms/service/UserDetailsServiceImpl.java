package org.gms.service;

import org.gms.dao.entity.AccountsDO;
import org.gms.dao.mapper.AccountsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountsMapper userDao;

    @Autowired
    public UserDetailsServiceImpl(AccountsMapper userRepository) {
        this.userDao = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountsDO user = userDao.selectOneByName(username);
        if (user == null) {return null;}

        if (user.getWebadmin() != null && user.getWebadmin() == 1) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return UserDetailsImpl.build(user, authorities);
        }
        return null;
    }

}
