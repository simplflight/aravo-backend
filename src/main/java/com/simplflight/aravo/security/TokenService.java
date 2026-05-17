package com.simplflight.aravo.security;

import com.simplflight.aravo.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.jwt.access-token-expires-in}")
    private Long accessTokenExpiresIn;

    @Value("${api.security.jwt.refresh-token-expires-in}")
    private Long refreshTokenExpiresIn;

    // Gera a chave criptográfica a partir de "secret"
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiresIn);
        String type = "access";

        return buildToken(user, type, now, expiryDate);
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiresIn);
        String type = "refresh";

        return buildToken(user, type, now, expiryDate);
    }

    public String validateTokenAndGetSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .require("type", "access")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String validateRefreshToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .require("type", "refresh")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private String buildToken(User user, String type, Date now, Date expiryDate) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId().toString())
                .claim("nickname", user.getNickname())
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey()) // Assinatura digital com chave
                .compact();
    }
}
