package org.gms.aop;

import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.gms.dao.entity.AuditLogDO;
import org.gms.exception.BizExceptionEnum;
import org.gms.model.dto.ResultBody;
import org.gms.model.dto.SubmitBody;
import org.gms.service.AuditLogService;
import org.gms.service.UserDetailsImpl;
import org.gms.util.RequireUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Aspect
@Component
@AllArgsConstructor
public class AuditLogAspect {
    private static final int PARAMS_MAX_LENGTH = 2000;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(\"\\w*password\\w*\"\\s*:\\s*\")[^\"]*(\")");

    private final AuditLogService auditLogService;

    @Around("execution(* org.gms.controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null || "GET".equalsIgnoreCase(request.getMethod())) {
            return pjp.proceed();
        }

        long start = System.currentTimeMillis();
        AuditLogDO auditLog = buildAuditLog(pjp, request);
        try {
            Object ret = pjp.proceed();
            if (ret instanceof ResultBody<?> body) {
                boolean success = BizExceptionEnum.SUCCESS.getResultCode().equals(body.getCode());
                auditLog.setResult(success ? 1 : 0);
                if (!success) {
                    auditLog.setErrorMsg(body.getMessage());
                }
            } else {
                auditLog.setResult(1);
            }
            return ret;
        } catch (Throwable e) {
            auditLog.setResult(0);
            auditLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            auditLog.setCostMs((int) (System.currentTimeMillis() - start));
            auditLogService.asyncSave(auditLog);
        }
    }

    private AuditLogDO buildAuditLog(ProceedingJoinPoint pjp, HttpServletRequest request) {
        AuditLogDO auditLog = new AuditLogDO();
        auditLog.setIp(resolveIp(request));
        auditLog.setHttpMethod(request.getMethod());
        auditLog.setUri(request.getRequestURI());
        auditLog.setOperation(resolveOperation(pjp));
        auditLog.setParams(serializeParams(pjp.getArgs()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl user) {
            auditLog.setOperatorId(user.getId());
            auditLog.setOperatorName(user.getUsername());
        } else {
            // /auth/login 等无token接口，从请求体中取尝试登录的用户名
            auditLog.setOperatorName(resolveLoginUsername(pjp.getArgs()));
        }
        return auditLog;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String resolveOperation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Operation operation = signature.getMethod().getAnnotation(Operation.class);
        if (operation != null && !RequireUtil.isEmpty(operation.summary())) {
            return operation.summary();
        }
        return signature.getDeclaringType().getSimpleName() + "#" + signature.getName();
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!RequireUtil.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int commaIndex = ip.indexOf(',');
            return commaIndex > 0 ? ip.substring(0, commaIndex).trim() : ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (!RequireUtil.isEmpty(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private String resolveLoginUsername(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof SubmitBody<?> body && body.getData() instanceof Map<?, ?> map) {
                Object username = map.get("username");
                if (username != null) {
                    return username.toString();
                }
            }
        }
        return null;
    }

    private String serializeParams(Object[] args) {
        try {
            List<Object> serializableArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg instanceof ServletRequest || arg instanceof ServletResponse
                        || arg instanceof MultipartFile) {
                    continue;
                }
                serializableArgs.add(arg);
            }
            String json = JSON.toJSONString(serializableArgs);
            json = PASSWORD_PATTERN.matcher(json).replaceAll("$1***$2");
            if (json.length() > PARAMS_MAX_LENGTH) {
                json = json.substring(0, PARAMS_MAX_LENGTH);
            }
            return json;
        } catch (Exception e) {
            return null;
        }
    }
}
