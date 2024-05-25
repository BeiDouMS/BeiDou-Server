package org.gms.controller;

import lombok.AllArgsConstructor;
import org.gms.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {
    private final TestService testService;
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        return testService.test();
    }

    @RequestMapping(value = "/testAccount", method = RequestMethod.GET)
    public String testAccount() {
        return testService.testAccount();
    }

    @RequestMapping(value = "/testPackage", method = RequestMethod.GET)
    public void testPackage() {
        testService.testPackage();
    }
}
