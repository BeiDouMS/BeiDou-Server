package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.gms.client.DefaultDates;
import org.gms.config.YamlConfig;
import org.gms.dao.entity.AccountsDO;
import org.gms.dao.mapper.AccountsMapper;
import org.gms.dto.*;
import org.gms.tools.BCrypt;
import org.gms.tools.HexTool;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

import static org.gms.client.Client.LOGIN_LOGGEDIN;

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

    public AccountsDO findById(int id) {
        return accountsMapper.selectOneById(id);
    }

    public AccountsDO getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findByName(userDetails.getUsername());
    }

    public Page<AccountsDO> getAccountList(Integer page,
                                           Integer size,
                                           Integer id,
                                           String name,
                                           String lastLoginStart,
                                           String lastLoginEnd,
                                           String createdAtStart,
                                           String createdAtEnd) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (id != null) queryWrapper.eq("id", id);
        if (name != null) queryWrapper.like("name", name);
        if (lastLoginStart != null) queryWrapper.ge(AccountsDO::getLastlogin, lastLoginStart);
        if (lastLoginEnd != null) queryWrapper.le(AccountsDO::getLastlogin, lastLoginEnd);
        if (createdAtStart != null) queryWrapper.ge(AccountsDO::getCreatedat, createdAtStart);
        if (createdAtEnd != null) queryWrapper.le(AccountsDO::getCreatedat, createdAtEnd);

        if (page == null) page = 1;
        if (size == null) size = Integer.MAX_VALUE;
        return accountsMapper.paginateWithRelations(page, size, queryWrapper);
    }

    public void addAccount(AddAccountDTO submitData) throws NoSuchAlgorithmException {
        // 防止swagger调用，后续的语言路由都受影响
        RequireUtil.requireNotNull(submitData.getLanguage(), I18nUtil.getExceptionMessage("LANGUAGE_NOT_SUPPORT"));
        RequireUtil.requireNull(findByName(submitData.getName()), I18nUtil.getExceptionMessage("AccountService.addAccount.exception1"));
        AccountsDO account = AccountsDO.builder()
                .name(submitData.getName())
                .password(encryptPassword(submitData.getPassword()))
                .birthday(submitData.getBirthday())
                .tempban(Timestamp.valueOf(DefaultDates.getTempban()))
                .language(submitData.getLanguage())
                .build();
        // 可以直接用insertSelective忽略null值
        accountsMapper.insertSelective(account);
    }

    public void updateAccountByUser(UpdateAccountByUserDTO submitData) throws NoSuchAlgorithmException {
        AccountsDO account = getCurrentUser();
        RequireUtil.requireTrue(checkPassword(submitData.getOldPwd(), account), I18nUtil.getExceptionMessage("AccountService.updateAccountByUser.oldPassword"));
        // 防止swagger调用，后续的语言路由都受影响
        RequireUtil.requireNotNull(submitData.getLanguage(), I18nUtil.getExceptionMessage("LANGUAGE_NOT_SUPPORT"));

        AccountsDO newData = new AccountsDO();
        if (submitData.getNewPwd() != null && submitData.getNewPwd().length() >= 6) {
            newData.setPassword(encryptPassword(submitData.getNewPwd()));
        }
        newData.setPin(submitData.getPin());
        newData.setPic(submitData.getPic());
        newData.setBirthday(submitData.getBirthday());
        newData.setNick(submitData.getNick());
        newData.setEmail(submitData.getEmail());
        newData.setLanguage(submitData.getLanguage());

        accountsMapper.update(newData);
    }

    public void updateAccountByGM(int id, UpdateAccountByGmDTO submitData) throws NoSuchAlgorithmException {
        AccountsDO account = findById(id);
        RequireUtil.requireNotNull(account, I18nUtil.getExceptionMessage("AccountService.id.NotExist"));
        // 防止swagger调用，后续的语言路由都受影响
        RequireUtil.requireNotNull(account.getLanguage(), I18nUtil.getExceptionMessage("LANGUAGE_NOT_SUPPORT"));
        RequireUtil.requireFalse(account.getLoggedin() == LOGIN_LOGGEDIN, I18nUtil.getExceptionMessage("AccountService.isOnline"));
        if (submitData.getNewPwd() != null && submitData.getNewPwd().length() >= 6) {
            account.setPassword(encryptPassword(submitData.getNewPwd()));
        }
        account.setPin(submitData.getPin());
        account.setPic(submitData.getPic());
        account.setBirthday(submitData.getBirthday());
        account.setNxCredit(submitData.getNxCredit());
        account.setMaplePoint(submitData.getMaplePoint());
        account.setNxPrepaid(submitData.getNxPrepaid());
        account.setCharacterslots(submitData.getCharacterslots());
        account.setGender(submitData.getGender());
        account.setWebadmin(submitData.getWebadmin());
        account.setNick(submitData.getNick());
        account.setMute(submitData.getMute());
        account.setEmail(submitData.getEmail());
        account.setRewardpoints(submitData.getRewardpoints());
        account.setVotepoints(submitData.getVotepoints());
        account.setLanguage(submitData.getLanguage());

        accountsMapper.update(account);
    }

    public void deleteAccountByGM(int id) {
        RequireUtil.requireNotNull(findById(id), I18nUtil.getExceptionMessage("AccountService.id.NotExist"));
        accountsMapper.deleteById(id);
    }

    public String encryptPassword(String password) throws NoSuchAlgorithmException {
        return YamlConfig.config.server.BCRYPT_MIGRATION ? BCrypt.hashpw(password, BCrypt.gensalt(12)) : BCrypt.hashpwSHA512(password);
    }

    public boolean checkPassword(String pwd, AccountsDO accountsDO) {
        String passHash = accountsDO.getPassword();
        if (passHash.charAt(0) == '$' && passHash.charAt(1) == '2' && BCrypt.checkpw(pwd, passHash)) {
            return true;
        } else {
            return pwd.equals(passHash) || checkHash(passHash, "SHA-1", pwd) || checkHash(passHash, "SHA-512", pwd);
        }
    }

    private static boolean checkHash(String hash, String type, String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes(StandardCharsets.UTF_8), 0, password.length());
            return HexTool.toHexString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }
}
