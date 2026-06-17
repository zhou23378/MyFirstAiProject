package com.salon.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类
 * <p>
 * 提供 JWT Token 的生成和解析功能
 * </p>
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT 密钥，从配置文件中读取 */
    private final SecretKey secretKey;

    /** Token 过期时间（毫秒），默认 24 小时 */
    private final long expiration;

    /**
     * 构造 JWT 工具类
     *
     * @param secret     JWT 密钥字符串
     * @param expiration Token 过期时间（毫秒）
     */
    private static final int MIN_KEY_BYTES = 32;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration:86400000}") long expiration) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalArgumentException(
                "JWT secret too short: " + keyBytes.length + " bytes, minimum " + MIN_KEY_BYTES + " required");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param adminId  管理员ID
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateToken(Long adminId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(adminId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中解析 Claims
     *
     * @param token JWT Token
     * @return Claims，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取管理员ID
     *
     * @param token JWT Token
     * @return 管理员ID，解析失败返回 null
     */
    public Long getAdminId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名，解析失败返回 null
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从 Token 中获取角色
     *
     * @param token JWT Token
     * @return 角色，解析失败返回 null
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT Token
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}
