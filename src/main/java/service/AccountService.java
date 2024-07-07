package service;

import api.exception.ExceptionEnum;
import client.Character;
import client.Client;
import client.DefaultDates;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import config.YamlConfig;
import dao.entity.AccountsEntity;
import dao.entity.CharactersEntity;
import dao.entity.IpbansEntity;
import dao.entity.MacbansEntity;
import dao.mapper.AccountsMapper;
import dao.mapper.CharactersMapper;
import dao.mapper.IpbansMapper;
import dao.mapper.MacbansMapper;
import dto.AddAccountDTO;
import dto.UpdateAccountByGmDTO;
import dto.UpdateAccountByUserDTO;
import net.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tools.BCrypt;
import tools.HexTool;
import utils.RequireUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;

import static client.Client.LOGIN_LOGGEDIN;
import static client.Client.LOGIN_NOTLOGGEDIN;

@Service
public class AccountService {
    private final AccountsMapper accountsMapper;
    private final CharactersMapper charactersMapper;
    private final IpbansMapper ipbansMapper;
    private final MacbansMapper macbansMapper;

    @Autowired
    public AccountService(AccountsMapper accountsMapper, CharactersMapper charactersMapper, IpbansMapper ipbansMapper, MacbansMapper macbansMapper) {
        this.accountsMapper = accountsMapper;
        this.charactersMapper = charactersMapper;
        this.ipbansMapper = ipbansMapper;
        this.macbansMapper = macbansMapper;
    }

    public AccountsEntity findByName(String name) {
        return accountsMapper.selectOneByName(name);
    }

    public AccountsEntity findById(int id) {
        return accountsMapper.selectOneById(id);
    }

    public AccountsEntity getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findByName(userDetails.getUsername());
    }

    public Page<AccountsEntity> getAccountList(Integer page,
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
        if (lastLoginStart != null) queryWrapper.ge(AccountsEntity::getLastlogin, lastLoginStart);
        if (lastLoginEnd != null) queryWrapper.le(AccountsEntity::getLastlogin, lastLoginEnd);
        if (createdAtStart != null) queryWrapper.ge(AccountsEntity::getCreatedat, createdAtStart);
        if (createdAtEnd != null) queryWrapper.le(AccountsEntity::getCreatedat, createdAtEnd);

        if (page == null) page = 1;
        if (size == null) size = Integer.MAX_VALUE;
        return accountsMapper.paginateWithRelations(page, size, queryWrapper);
    }

    public void addAccount(AddAccountDTO submitData) throws NoSuchAlgorithmException {
        RequireUtil.requireNull(findByName(submitData.getName()), ExceptionEnum.REPEAT_USERNAME);
        AccountsEntity account = AccountsEntity.builder()
                .name(submitData.getName())
                .password(encryptPassword(submitData.getPassword()))
                .birthday(submitData.getBirthday())
                .tempban(Timestamp.valueOf(DefaultDates.getTempban()))
                .build();
        // 可以直接用insertSelective忽略null值
        accountsMapper.insertSelective(account);
    }

    public void updateAccountByUser(UpdateAccountByUserDTO submitData) throws NoSuchAlgorithmException {
        AccountsEntity account = getCurrentUser();
        RequireUtil.requireTrue(checkPassword(submitData.getOldPwd(), account), ExceptionEnum.NOT_FOUND, "旧密码错误");

        AccountsEntity newData = new AccountsEntity();
        newData.setId(account.getId());
        if (submitData.getNewPwd() != null && submitData.getNewPwd().length() >= 6) {
            newData.setPassword(encryptPassword(submitData.getNewPwd()));
        }
        newData.setPin(submitData.getPin());
        newData.setPic(submitData.getPic());
        newData.setBirthday(submitData.getBirthday());
        newData.setNick(submitData.getNick());
        newData.setEmail(submitData.getEmail());

        accountsMapper.update(newData);
    }

    public void updateAccountByGM(int id, UpdateAccountByGmDTO submitData) throws NoSuchAlgorithmException {
        AccountsEntity account = findById(id);
        RequireUtil.requireNotNull(account, ExceptionEnum.NOT_FOUND, "用户id不存在");
        RequireUtil.requireFalse(account.getLoggedin() == LOGIN_LOGGEDIN, ExceptionEnum.ACCOUNT_IS_ONLINE);
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

        accountsMapper.update(account);
    }

    public void deleteAccountByGM(int id) {
        RequireUtil.requireNotNull(findById(id), ExceptionEnum.NOT_FOUND, "用户id不存在");
        accountsMapper.deleteById(id);
    }

    public String encryptPassword(String password) throws NoSuchAlgorithmException {
        return YamlConfig.config.server.BCRYPT_MIGRATION ? BCrypt.hashpw(password, BCrypt.gensalt(12)) : BCrypt.hashpwSHA512(password);
    }

    public boolean checkPassword(String pwd, AccountsEntity AccountsEntity) {
        String passHash = AccountsEntity.getPassword();
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

    public void resetLoggedIn(int id) {
        RequireUtil.requireNotNull(findById(id), ExceptionEnum.NOT_FOUND, "用户id不存在");

        AccountsEntity account = new AccountsEntity();
        account.setId(id);
        account.setLoggedin(LOGIN_NOTLOGGEDIN);
        accountsMapper.update(account);
    }

    public void banAccount(int accountId, String reason) {
        RequireUtil.requireNotNull(findById(accountId), ExceptionEnum.NOT_FOUND, "用户id不存在");

        // 封停账号
        AccountsEntity account = new AccountsEntity();
        account.setId(accountId);
        account.setBanned(true);
        account.setBanreason(reason);
        accountsMapper.update(account);
        // 遍历账号下的角色，如果在线，追封客户端/Mac/IP
        List<CharactersEntity> characterList = charactersMapper.selectIdAndWorldListByAccountId(accountId); // 仅查询角色ID和所在world
        for (CharactersEntity chr : characterList) {
            Character player = Server.getInstance()
                    .getWorlds()
                    .get(chr.getWorld())
                    .getPlayerStorage()
                    .getCharacterById(chr.getId());
            if (player == null) return; // 角色离线

            Client c = player.getClient(); // 角色在线，获取客户端
            c.banMacs(); // 封禁Mac
            // c.banHWID(); // 封禁客户端 操作不可逆？
            // 封禁IP
            String ip = c.getRemoteAddress();
            IpbansEntity ipban = IpbansEntity.builder().ip(ip).aid(String.valueOf(accountId)).build();
            ipbansMapper.insert(ipban);
            // 强制离线，这个方法只是中断了连接不会造成客户端退出，但是实际跟掉线没什么区别
            c.disconnect(false, false);
        }
    }

    public void unbanAccount(int accountId) {
        RequireUtil.requireNotNull(findById(accountId), ExceptionEnum.NOT_FOUND, "用户id不存在");

        // 解封账号
        AccountsEntity account = new AccountsEntity();
        account.setId(accountId);
        account.setBanned(false);
        accountsMapper.update(account);
        // 解封Mac
        macbansMapper.deleteByQuery(new QueryWrapper().eq(MacbansEntity::getAid, accountId));
        // 解封Ip
        ipbansMapper.deleteByQuery(new QueryWrapper().eq(IpbansEntity::getAid, accountId));
    }
}
