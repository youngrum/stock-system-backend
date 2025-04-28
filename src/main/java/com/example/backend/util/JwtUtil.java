package com.example.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component; // これも追加！

@Component
public class JwtUtil {

    // ★ ここ！シンプルな秘密鍵にしておく
    private static final String SECRET_KEY = "your-super-strong-secret-key-your-super-strong-secret-key";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // トークンの有効期限（30分）
    private static final long EXPIRATION_TIME = 1000 * 60 * 30; // 30分（ミリ秒）

    // トークン生成
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey()) // ★ここも getSigningKey() で署名する
                .compact();
    }

    // トークンをパースしてClaimsを取得
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
