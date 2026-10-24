package org.gms.service;

import org.gms.net.server.services.task.world.CharacterSaveService;
import org.gms.net.server.coordinator.session.SessionCoordinator;
import org.gms.net.server.coordinator.session.SessionCoordinator.AccountSession;
import org.gms.client.Client;
import org.gms.exception.BizException;
import org.gms.exception.BizExceptionEnum;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.DefaultDates;
import org.gms.config.GameConfig;
import org.gms.dao.entity.*;
import org.gms.dao.mapper.*;
import org.gms.model.dto.AddAccountDTO;
import org.gms.model.dto.UpdateAccountByGmDTO;
import org.gms.model.dto.UpdateAccountByUserDTO;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.server.maps.HiredMerchant;
import org.gms.util.BCrypt;
import org.gms.util.HexTool;
import org.gms.util.I18nUtil;
import org.gms.util.RequireUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;
import java.util.NoSuchElementException;

import static org.gms.client.Client.LOGIN_LOGGEDIN;
import static org.gms.client.Client.LOGIN_NOTLOGGEDIN;
import static org.gms.dao.entity.table.CharactersDOTableDef.CHARACTERS_D_O;
import static org.gms.dao.entity.table.IpbansDOTableDef.IPBANS_D_O;

@Service
@AllArgsConstructor
@Slf4j
public class AccountService {
    private final AccountsMapper accountsMapper;
    private final CharactersMapper charactersMapper;
    private final IpbansMapper ipbansMapper;
    private final MacbansMapper macbansMapper;
    private final QuickslotkeymappedMapper quickslotkeymappedMapper;

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

    public void update(AccountsDO condition) {
        if (accountsMapper.update(condition) > 0
                && (Boolean.TRUE.equals(condition.getBanned())
                || condition.getTempban() != null && condition.getTempban().getTime() > System.currentTimeMillis())) {
            closeAccountHiredMerchants(condition.getId());
        }
    }

    /** 雇佣商店不依赖角色在线，按账号全角色、全世界收店。 */
    public void closeAccountHiredMerchants(int accountId) {
        List<CharactersDO> owners;
        try {
            owners = charactersMapper.selectIdAndWorldListByAccountId(accountId);
        } catch (RuntimeException e) {
            log.error(I18nUtil.getLogMessage("AccountService.closeMerchants.lookupFailed", accountId), e);
            return;
        }
        for (World world : Server.getInstance().getWorlds()) {
            for (CharactersDO owner : owners) {
                try {
                    HiredMerchant merchant = world.getHiredMerchant(owner.getId());
                    if (merchant != null) merchant.closeForBan();
                } catch (RuntimeException e) {
                    log.error(I18nUtil.getLogMessage("AccountService.closeMerchants.closeFailed", accountId, owner.getId()), e);
                }
            }
        }
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
                .lastlogin(Timestamp.valueOf(DefaultDates.getTempban()))
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
        newData.setId(account.getId());
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

    public String encryptPassword(String password) throws NoSuchAlgorithmException {
        return GameConfig.getServerBoolean("bcrypt_migration") ? BCrypt.hashpw(password, BCrypt.gensalt(12)) : BCrypt.hashpwSHA512(password);
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

    public void resetAllLoggedIn(int id) {
        RequireUtil.requireNotNull(findById(id), I18nUtil.getExceptionMessage("AccountService.id.NotExist"));
        AccountSession account = SessionCoordinator.getInstance().getAccountSession(id);
        synchronized (account) {
            Client registered = SessionCoordinator.getInstance().getClientByAccID(id);
            if ((registered != null && registered.hasActiveConnection())
                    || CharacterSaveService.getInstance().hasUnfinishedSession(id) || Client.hasPendingDisconnectForAccount(id)) {
                throw new BizException(BizExceptionEnum.CHARACTER_SAVE_UNAVAILABLE);
            }
            AccountsDO value = new AccountsDO(); value.setId(id); value.setLoggedin(LOGIN_NOTLOGGEDIN);
            accountsMapper.update(value);
            account.retire();
        }
    }

    public void banAccount(int accountId, String reason) {
        RequireUtil.requireNotNull(findById(accountId), I18nUtil.getExceptionMessage("AccountService.id.NotExist"));

        // 封停账号
        AccountsDO account = new AccountsDO();
        account.setId(accountId);
        account.setBanned(true);
        account.setBanreason(reason);
        update(account);
        // 遍历账号下的角色，如果在线，追封客户端/Mac/IP
        List<CharactersDO> characterList = charactersMapper.selectIdAndWorldListByAccountId(accountId); // 仅查询角色ID和所在world
        for (CharactersDO chr : characterList) {
            World world = Server.getInstance().getWorld(chr.getWorld());
            if (world == null) continue;
            Character player = world.getPlayerStorage().getCharacterById(chr.getId());
            if (player == null) continue; // 角色离线
            player.setBanned(true);
            Client c = player.getClient(); // 角色在线，获取客户端
            c.banMacs(); // 封禁Mac
            // c.banHWID(); // 封禁客户端 操作不可逆？
            // 封禁IP
            String ip = c.getRemoteAddress();
            IpbansDO ipban = IpbansDO.builder().ip(ip).aid(String.valueOf(accountId)).build();
            ipbansMapper.insertSelective(ipban);
            // 强制离线，这个方法只是中断了连接不会造成客户端退出，但是实际跟掉线没什么区别
            c.disconnect(false, false);
        }
    }

    public void unbanAccount(int accountId) {
        RequireUtil.requireNotNull(findById(accountId), I18nUtil.getExceptionMessage("AccountService.id.NotExist"));

        // 解封账号
        AccountsDO account = new AccountsDO();
        account.setId(accountId);
        account.setBanned(false);
        accountsMapper.update(account);
        // 解封Mac
        macbansMapper.deleteByQuery(new QueryWrapper().eq(MacbansDO::getAid, accountId));
        // 解封Ip
        ipbansMapper.deleteByQuery(new QueryWrapper().eq(IpbansDO::getAid, accountId));
    }

    public void resetAllLoggedIn() {
        if (CharacterSaveService.getInstance().hasUnfinishedSessions()) throw new BizException(BizExceptionEnum.CHARACTER_SAVE_UNAVAILABLE);
        accountsMapper.updateAllLoggedIn(0);
    }

    public void ban(Character chr, String reason) {
        update(AccountsDO.builder().banned(true).id(chr.getAccountId()).banreason(reason).build());
        // 更新在线的ban状态
        chr.setBanned(true);
    }

    public void ban(String str, String reason, boolean isAccount) {
        if (str.matches("[0-9]{1,3}\\..*")) {
            if (isBanned(str)) {
                return;
            }
            ipbansMapper.insertSelective(IpbansDO.builder().ip(str).build());
            return;
        }
        Integer accountId = null;
        if (isAccount) {
            AccountsDO accountsDO = findByName(str);
            if (accountsDO != null) {
                accountId = accountsDO.getId();
            }
        } else {
            List<CharactersDO> charactersDOS = charactersMapper.selectListByQuery(QueryWrapper.create().where(CHARACTERS_D_O.NAME.eq(str)));
            if (!charactersDOS.isEmpty()) {
                accountId = charactersDOS.getFirst().getAccountid();
            }
        }
        if (accountId == null) {
            throw new NoSuchElementException();
        }
        update(AccountsDO.builder()
                .id(accountId)
                .banreason(reason)
                .banned(true)
                .build());
    }

    public boolean isBanned(String ip) {
        return ipbansMapper.selectCountByQuery(QueryWrapper.create().where(IPBANS_D_O.IP.eq(ip))) > 0;
    }

    public QuickslotkeymappedDO getQuickSlotKeyMap(int accountId) {
        return quickslotkeymappedMapper.selectOneById(accountId);
    }
}
