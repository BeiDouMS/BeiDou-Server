package org.gms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gms.dao.entity.AccountsDO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountController {

    @Tag(name = "v1")
    @Operation(summary = "查询账号")
    @PostMapping("/v1/account/search")
    public List<AccountsDO> searchAccount() {
        return null;
    }

    @Tag(name = "v1")
    @Operation(summary = "修改账号")
    @PostMapping("/v1/account/modify")
    public AccountsDO modifyAccount(String accountId) {
        return null;
    }
}
