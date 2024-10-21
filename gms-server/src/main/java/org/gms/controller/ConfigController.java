package org.gms.controller;

import lombok.AllArgsConstructor;
import org.gms.service.ConfigService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/config")
public class ConfigController {
    private final ConfigService configService;


}
