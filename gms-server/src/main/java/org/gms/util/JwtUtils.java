package org.gms.util;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.duration}")
    private int jwtDuration;

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject((username))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtDuration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("访问者的Token签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("访问者的Token无效: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("访问者的Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("访问者的Token不被支持: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("访问者的Token参数为空: {}", e.getMessage());
        }

        return false;
    }
}
