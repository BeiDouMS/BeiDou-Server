package org.gms.aop;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.gms.exception.BizException;
import org.gms.service.AccountService;
import org.gms.service.UserDetailsServiceImpl;
import org.gms.util.JwtUtils;
import org.gms.util.RateLimitUtil;
import org.gms.util.RequireUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired(required = false)
    private SpringDocConfigProperties springDocConfigProperties;
    @Autowired(required = false)
    private SwaggerUiConfigProperties swaggerUiConfigProperties;
    @Autowired
    private AccountService accountService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
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
                throw new BizException("Rate limit");
            }

            String jwt = parseJwt(request);
            // 测试token，所以生产环境一定要把swagger关掉，否则裸奔
            if (springDocConfigProperties!=null && swaggerUiConfigProperties!=null  && "swagger".equals(jwt) && springDocConfigProperties.getApiDocs().isEnabled() && swaggerUiConfigProperties.isEnabled()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Filter error", e);
            // 释放流，否则可能内存泄漏
            request.getInputStream().close();
            response.getOutputStream().close();
            return;
        }
        // 替换成允许多次读取的HttpServletRequest
        filterChain.doFilter(new CachedHttpServletRequest(request), response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
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
