package com.safa.logisticintegration.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

@Component
public class TokenManager implements Serializable {
    public static final int TOKEN_VALIDITY_DAYS = 100;
    @Serial
    private static final long serialVersionUID = 7008375124389347049L;

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    public Boolean validateJwtToken(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(generalKey()).build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        Calendar currentTimeNow = Calendar.getInstance();
        boolean isTokenExpired = claims.getExpiration().before(currentTimeNow.getTime());
        return !isTokenExpired;
    }

    public SecretKey generalKey() {
        return new SecretKeySpec(jwtSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(generalKey()).build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
