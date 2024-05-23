package org.gms.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.gms.api.BaseController;
import org.gms.api.dto.BaseNettyRequest;
import org.gms.api.dto.BaseNettyResponse;

@Slf4j
public class TestController implements BaseController {

    @Override
    public BaseNettyResponse<?> request(BaseNettyRequest<?> request) {
        return BaseNettyResponse.<String>builder()
                .responseId(request.getRequestId())
                .responseCode("success")
                .responseData("this is gms-server's test")
                .build();
    }
}
