package com.cloudnote.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 工具(jjwt 0.12.x)
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //=================== 密码兼容工具 ====================
    // 旧系统密码: MD5摘要 + Base64; 新系统统一 BCrypt; 登录时两种均可验证

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** 新密码一律 BCrypt 存储 */
    public static String bcrypt(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matchesBcrypt(String raw, String encoded) {
        return ENCODER.matches(raw, encoded);
    }

    /** 旧密码哈希: base64(md5(raw)) */
    public static String legacyMd5Base64(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("密码加密失败", e);
        }
    }

    /** 判断存量密码是否已迁移为 BCrypt */
    public static boolean isBcrypt(String stored) {
        return stored != null && stored.startsWith("$2");
    }
}
