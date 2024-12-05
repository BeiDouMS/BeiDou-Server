package org.gms.aop;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.exception.BizException;
import org.gms.service.AccountService;
import org.gms.util.RateLimitUtil;
import org.gms.util.RequireUtil;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 处理业务类的过滤器，到了这里的请求已经经过security的过滤
 */
@Slf4j
@Component
@AllArgsConstructor
public class ServerFilter extends HttpFilter {
    private final AccountService accountService;

    protected boolean shouldNotFilter(final HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // web resource
        if (requestURI.startsWith("/assets")) {
            return true;
        }
        // swagger
        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
            return true;
        }
        return "/".equals(requestURI);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String forwardedIp = request.getHeader("X-Forwarded-For");
            String realIp = request.getHeader("X-Real-IP");
            String remoteAddr = request.getRemoteAddr();
            if (RequireUtil.isEmpty(remoteAddr)) remoteAddr = forwardedIp;
            if (RequireUtil.isEmpty(remoteAddr)) remoteAddr = realIp;
            RequireUtil.requireNotEmpty(remoteAddr, "Unknown remote address");

            // 封禁ip禁止请求
            if (accountService.isBanned(remoteAddr)) {
                request.getInputStream().close();
                throw new BizException("Banned ip is requesting, forwardedIp: " + forwardedIp + ",realIp: " + realIp + ", remoteAddr: " + remoteAddr);
            }

            // 限流
            if (!RateLimitUtil.getInstance().check(remoteAddr)) {
                throw new BizException("IP " + remoteAddr + " has reached rate limit.");
            }
        } catch (Exception e) {
            log.error("Filter error", e);
            // 释放流，否则可能内存泄漏
            request.getInputStream().close();
            response.getOutputStream().close();
            return;
        }
        // 这一步 应该在限流之后进行
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }
        if (request.getContentType() == null || request.getContentType().contains("multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }
        // 替换成允许多次读取的HttpServletRequest
        chain.doFilter(new CachedHttpServletRequest(request), response);
    }

    private static class CachedHttpServletRequest extends HttpServletRequestWrapper {

        private byte[] cachedBody;

        public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            cacheRequestBody(request);
        }

        private void cacheRequestBody(HttpServletRequest request) throws IOException {
            InputStream requestInputStream = request.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = requestInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            this.cachedBody = byteArrayOutputStream.toByteArray();
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedServletInputStream(this.cachedBody);
        }
    }

    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream byteArrayInputStream;

        public CachedServletInputStream(byte[] cachedBody) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() {
            return byteArrayInputStream.read();
        }
    }
}
