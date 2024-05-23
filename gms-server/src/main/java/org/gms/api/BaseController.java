package org.gms.api;

import org.gms.api.dto.BaseNettyRequest;
import org.gms.api.dto.BaseNettyResponse;

public interface BaseController {
    BaseNettyResponse<?> request(BaseNettyRequest<?> request);
}
