package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.constants.api.ApiConstant;
import org.gms.dto.AccountsSearchDto;
import org.gms.dto.ResultBody;
import org.gms.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResultBody info() {
        return ResultBody.success(accountService.getCurrentUser());
    }

    @Tag(name = "/account/" + ApiConstant.LATEST)
    @Operation(summary = "分页查询账号列表")
    @PostMapping("/" + ApiConstant.LATEST + "/searchByPage")
    public ResultBody create(AccountsSearchDto accountsSearchDto) {
        return ResultBody.success(accountService.searchUserList(accountsSearchDto));
    }
}
