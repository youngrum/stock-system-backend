package com.example.backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // 秘密鍵（本番では環境変数などに分離する！）
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // トークンの有効期限（例：30分）
    private static final long EXPIRATION_TIME = 1000 * 60 * 30; // ミリ秒

    public static String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
