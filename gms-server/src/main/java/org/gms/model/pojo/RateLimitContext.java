package org.gms.model.pojo;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class RateLimitContext {
    private AtomicInteger curr;
    private Long expire;
}
