package com.example.weather_rest_clone.service.util;

import com.example.weather_rest_clone.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.signing.key}")
    private String signingKey;

    @Value("${jwt-token.live-time-in-second}")
    private Long jwtLiveTimeInSecond;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateNewJwtToken(User user) {
        LocalDateTime expirationTime = LocalDateTime.now().plusSeconds(jwtLiveTimeInSecond);
        var expirationTimeInDate =
                Date.from(expirationTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(claimMap)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationTimeInDate)
                .signWith(getSecretKey())
                .compact();
    }

    public Claims parseJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean doesTokenExpire(String token) {
        try {
            parseJwtToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
