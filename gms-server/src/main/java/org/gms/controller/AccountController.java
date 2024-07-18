package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.AccountsDO;
import org.gms.model.dto.*;
import org.gms.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "获取我的信息")
    @GetMapping("/" + ApiConstant.LATEST + "/info")
    public ResultBody<AccountsDO> info() {
        return ResultBody.success(accountService.getCurrentUser());
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "获取账号列表")
    @GetMapping("/" + ApiConstant.LATEST)
    public ResultBody<Page<AccountsDO>> getAccountList(@RequestParam(name = "page", required = false) Integer page,
                                                       @RequestParam(name = "size", required = false) Integer size,
                                                       @RequestParam(name = "id", required = false) Integer id,
                                                       @RequestParam(name = "name", required = false) String name,
                                                       @RequestParam(name = "lastLoginStart", required = false) String lastLoginStart,
                                                       @RequestParam(name = "lastLoginEnd", required = false) String lastLoginEnd,
                                                       @RequestParam(name = "createdAtStart", required = false) String createdAtStart,
                                                       @RequestParam(name = "createdAtEnd", required = false) String createdAtEnd) {
        return ResultBody.success(accountService.getAccountList(page, size, id, name, lastLoginStart, lastLoginEnd, createdAtStart, createdAtEnd));
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "注册账号")
    @PostMapping("/" + ApiConstant.LATEST)
    public ResultBody<Object> register(@RequestBody SubmitBody<AddAccountDTO> submitBody) throws NoSuchAlgorithmException {
        accountService.addAccount(submitBody.getData());
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "更新账号资料[用户](须校验旧密码,新密码留空则不修改)")
    @PutMapping("/" + ApiConstant.LATEST)
    public ResultBody<Object> updateByUser(@RequestBody SubmitBody<UpdateAccountByUserDTO> submitBody) throws NoSuchAlgorithmException {
        accountService.updateAccountByUser(submitBody.getData());
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "更新账号资料[GM]")
    @PutMapping("/" + ApiConstant.LATEST + "/{id}")
    public ResultBody<Object> updateByGm(@PathVariable("id") int id,
                                         @RequestBody SubmitBody<UpdateAccountByGmDTO> submitBody) throws NoSuchAlgorithmException {
        accountService.updateAccountByGM(id, submitBody.getData());
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "删除账号")
    @DeleteMapping("/" + ApiConstant.LATEST + "/{id}")
    public ResultBody<Object> delete(@PathVariable("id") int id) {
        accountService.deleteAccountByGM(id);
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "重置在线状态")
    @PutMapping("/" + ApiConstant.LATEST + "/{id}/reset/logged")
    public ResultBody<Object> resetLoggedIn(@PathVariable("id") int id) {
        accountService.resetAllLoggedIn(id);
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "封停账号")
    @PutMapping("/" + ApiConstant.LATEST + "/{id}/ban")
    public ResultBody<Object> banAccount(@PathVariable("id") int id,
                                         @RequestBody SubmitBody<Map<String, String>> submitBody) {
        accountService.banAccount(id, submitBody.getData().get("reason"));
        return ResultBody.success();
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "解封账号")
    @PutMapping("/" + ApiConstant.LATEST + "/{id}/unban")
    public ResultBody<Object> unbanAccount(@PathVariable("id") int id) {
        accountService.unbanAccount(id);
        return ResultBody.success();
    }
}
