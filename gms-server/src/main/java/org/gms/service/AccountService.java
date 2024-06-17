package org.gms.service;

import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SqlOperators;
import org.gms.constants.api.ModifyType;
import org.gms.dao.entity.AccountsDO;
import org.gms.dao.mapper.AccountsMapper;
import org.gms.dto.AccountModifyDTO;
import org.gms.dto.AccountsSearchDTO;
import org.gms.dto.SubmitBody;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import static org.gms.dao.entity.table.AccountsDOTableDef.ACCOUNTS_D_O;

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

    public Page<AccountsDO> searchUserList(SubmitBody<AccountsSearchDTO> submitBody) {
        AccountsSearchDTO accountsSearchDto = submitBody.getData();
        // 依据前端传入的字段查询，这2个时间字段做特殊处理
        SqlOperators operators = SqlOperators.of()
                .set(AccountsSearchDTO::getLastloginStart, SqlOperator.GE)
                .set(AccountsSearchDTO::getLastloginEnd, SqlOperator.LE)
                .set(AccountsSearchDTO::getCreatedatStart, SqlOperator.GE)
                .set(AccountsSearchDTO::getCreatedatEnd, SqlOperator.LE);

        QueryWrapper queryWrapper = QueryWrapper.create(accountsSearchDto, operators);
        return accountsMapper.paginate(new Page<>(), queryWrapper);
    }

    public boolean modifyAccount(SubmitBody<AccountModifyDTO> submitBody) {
        AccountModifyDTO accountModifyDTO = submitBody.getData();
        ModifyType modifyType = ModifyType.getByCode(accountModifyDTO.getModifyType());
        switch (modifyType) {
            case ModifyType.INSERT -> {
                AccountsDO result = accountsMapper.selectOneByName(accountModifyDTO.getName());
                RequireUtil.requireNull(result, I18nUtil.getExceptionMessage("AccountService.modifyAccount.exception1"));
                accountModifyDTO.setId(null);
                accountsMapper.insert(accountModifyDTO);
            }
            case ModifyType.UPDATE -> {
                if (accountModifyDTO.getId() == null) {
                    RequireUtil.requireNotEmpty(accountModifyDTO.getName(), I18nUtil.getExceptionMessage("AccountService.modifyAccount.exception2"));
                    AccountsDO accountsDO = AccountsDO.builder()
                            .password(accountModifyDTO.getPassword())
                            .pin(accountModifyDTO.getPin())
                            .pic(accountModifyDTO.getPic())
                            .loggedin(accountModifyDTO.getLoggedin())
                            .lastlogin(accountModifyDTO.getLastlogin())
                            .createdat(accountModifyDTO.getCreatedat())
                            .birthday(accountModifyDTO.getBirthday())
                            .banned(accountModifyDTO.getBanned())
                            .banreason(accountModifyDTO.getBanreason())
                            .macs(accountModifyDTO.getMacs())
                            .nxCredit(accountModifyDTO.getNxCredit())
                            .maplePoint(accountModifyDTO.getMaplePoint())
                            .nxPrepaid(accountModifyDTO.getNxPrepaid())
                            .characterslots(accountModifyDTO.getCharacterslots())
                            .gender(accountModifyDTO.getGender())
                            .tempban(accountModifyDTO.getTempban())
                            .greason(accountModifyDTO.getGreason())
                            .tos(accountModifyDTO.getTos())
                            .sitelogged(accountModifyDTO.getSitelogged())
                            .webadmin(accountModifyDTO.getWebadmin())
                            .nick(accountModifyDTO.getNick())
                            .mute(accountModifyDTO.getMute())
                            .email(accountModifyDTO.getEmail())
                            .ip(accountModifyDTO.getIp())
                            .rewardpoints(accountModifyDTO.getRewardpoints())
                            .votepoints(accountModifyDTO.getVotepoints())
                            .hwid(accountModifyDTO.getHwid())
                            .language(accountModifyDTO.getLanguage())
                            .build();
                    QueryWrapper queryWrapper = QueryWrapper.create().where(ACCOUNTS_D_O.NAME.eq(accountModifyDTO.getName()));
                    accountsMapper.updateByQuery(accountsDO, queryWrapper);
                } else {
                    accountsMapper.update(accountModifyDTO);
                }
            }
            case ModifyType.DELETE -> {
                if (accountModifyDTO.getId() == null) {
                    RequireUtil.requireNotEmpty(accountModifyDTO.getName(), I18nUtil.getExceptionMessage("AccountService.modifyAccount.exception2"));
                    QueryWrapper queryWrapper = QueryWrapper.create().where(ACCOUNTS_D_O.NAME.eq(accountModifyDTO.getName()));
                    accountsMapper.deleteByQuery(queryWrapper);
                } else {
                    accountsMapper.deleteById(accountModifyDTO.getId());
                }

            }
            default -> throw new UnsupportedOperationException();
        }
        return true;
    }
}
