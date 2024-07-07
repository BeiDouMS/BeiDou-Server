package api.controller;

import com.mybatisflex.core.paginate.Page;
import dao.entity.AccountsEntity;
import dto.AddAccountDTO;
import dto.UpdateAccountByGmDTO;
import dto.UpdateAccountByUserDTO;
import model.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.AccountService;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/info")
    public ResultBody<AccountsEntity> info() {
        return ResultBody.success(accountService.getCurrentUser());
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Page<AccountsEntity>> getAccountList(@RequestParam(name = "page", required = false) Integer page,
                                                           @RequestParam(name = "size", required = false) Integer size,
                                                           @RequestParam(name = "id", required = false) Integer id,
                                                           @RequestParam(name = "name", required = false) String name,
                                                           @RequestParam(name = "lastLoginStart", required = false) String lastLoginStart,
                                                           @RequestParam(name = "lastLoginEnd", required = false) String lastLoginEnd,
                                                           @RequestParam(name = "createdAtStart", required = false) String createdAtStart,
                                                           @RequestParam(name = "createdAtEnd", required = false) String createdAtEnd) {
        return ResultBody.success(accountService.getAccountList(page, size, id, name, lastLoginStart, lastLoginEnd, createdAtStart, createdAtEnd));
    }

    @PostMapping()
    public ResultBody<Object> register(@RequestBody AddAccountDTO submitData) throws NoSuchAlgorithmException {
        accountService.addAccount(submitData);
        return ResultBody.success();
    }

    @PutMapping()
    public ResultBody<Object> updateByUser(@RequestBody UpdateAccountByUserDTO submitData) throws NoSuchAlgorithmException {
        accountService.updateAccountByUser(submitData);
        return ResultBody.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> updateByGm(@PathVariable("id") int id,
                                         @RequestBody UpdateAccountByGmDTO submitData) throws NoSuchAlgorithmException {
        accountService.updateAccountByGM(id, submitData);
        return ResultBody.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> delete(@PathVariable("id") int id) {
        accountService.deleteAccountByGM(id);
        return ResultBody.success();
    }

    @PutMapping("/{id}/reset/logged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> resetLoggedIn(@PathVariable("id") int id) {
        accountService.resetLoggedIn(id);
        return ResultBody.success();
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> banAccount(@PathVariable("id") int id,
                                         @RequestBody Map<String, String> submitData) {
        accountService.banAccount(id, submitData.get("reason"));
        return ResultBody.success();
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultBody<Object> unbanAccount(@PathVariable("id") int id) {
        accountService.unbanAccount(id);
        return ResultBody.success();
    }
}
