package org.gms.util;

import lombok.extern.slf4j.Slf4j;
import org.gms.manager.ServerManager;
import org.gms.model.pojo.RateLimitContext;
import org.gms.property.ServiceProperty;
import org.gms.service.AccountService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RateLimitUtil {
    private static RateLimitUtil instance;
    private final ServiceProperty.RateLimitProperty rateLimitProperty;
    private final Map<String, RateLimitContext> contextMap;

    private RateLimitUtil() {
        rateLimitProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class).getRateLimit();
        contextMap = new HashMap<>();
    }

    public static RateLimitUtil getInstance() {
        if (instance == null) {
            instance = new RateLimitUtil();
        }
        return instance;
    }

    public boolean check(String ip) {
        if (!rateLimitProperty.isEnabled()) {
            return true;
        }
        try {
            RateLimitContext rateLimitContext = contextMap.get(ip);
            if (rateLimitContext == null) {
                rateLimitContext = new RateLimitContext();
                rateLimitContext.setCurr(new AtomicInteger(1));
                rateLimitContext.setExpire(System.currentTimeMillis() + rateLimitProperty.getDuration());
                contextMap.put(ip, rateLimitContext);
                return true;
            }
            if (rateLimitContext.getExpire() < System.currentTimeMillis()) {
                rateLimitContext.setExpire(System.currentTimeMillis() + rateLimitProperty.getDuration());
                rateLimitContext.getCurr().set(1);
                contextMap.put(ip, rateLimitContext);
                return true;
            }
            if (rateLimitContext.getCurr().incrementAndGet() > rateLimitProperty.getLimit()) {
                if (rateLimitProperty.isAutoBan()) {
                    AccountService accountService = ServerManager.getApplicationContext().getBean(AccountService.class);
                    accountService.ban(ip, "Auto banned by rate limit", true);
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Rate limit check error", e);
        }
        return false;
    }
}
