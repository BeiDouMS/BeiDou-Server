package org.gms.service;

import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SqlOperators;
import org.gms.dao.entity.AccountsDO;
import org.gms.dao.mapper.AccountsMapper;
import org.gms.dto.AccountsSearchDto;
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

    public Page<AccountsDO> searchUserList(AccountsSearchDto accountsSearchDto) {
        // 依据前端传入的字段查询，这2个时间字段做特殊处理
        SqlOperators operators = SqlOperators.of()
                .set(AccountsSearchDto::getLastloginStart, SqlOperator.GE)
                .set(AccountsSearchDto::getLastloginEnd, SqlOperator.LE)
                .set(AccountsSearchDto::getCreatedatStart, SqlOperator.GE)
                .set(AccountsSearchDto::getCreatedatEnd, SqlOperator.LE);

        QueryWrapper queryWrapper = QueryWrapper.create(accountsSearchDto, operators);
        return accountsMapper.paginate(new Page<>(), queryWrapper);
    }
}
