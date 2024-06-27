package org.gms.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.dao.entity.AccountsDO;
import org.gms.dto.AccountModifyDTO;
import org.gms.dto.AccountsSearchDTO;
import org.gms.dto.ResultBody;
import org.gms.dto.SubmitBody;
import org.gms.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "分页查询账号列表")
    @PostMapping("/" + ApiConstant.LATEST + "/searchByPage")
    public ResultBody<Page<AccountsDO>> searchByPage(@RequestBody SubmitBody<AccountsSearchDTO> submitBody) {
        return ResultBody.success(accountService.searchUserList(submitBody));
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "新增/修改/删除")
    @PostMapping("/" + ApiConstant.LATEST + "/modify")
    public ResultBody<Boolean> create(@RequestBody SubmitBody<AccountModifyDTO> submitBody) {
        return ResultBody.success(accountService.modifyAccount(submitBody));
    }
}
