package org.gms.controller;

import lombok.AllArgsConstructor;
import org.gms.service.CashShopService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashShop")
@AllArgsConstructor
public class CashShopController {
    private final CashShopService cashShopService;



}
