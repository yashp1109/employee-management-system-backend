package com.example.ems.security;

import com.example.ems.employee.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String createAccessToken(Employee employee) {
        return createToken(employee, accessTokenMinutes * 60, Map.of(
                "role", employee.getRole().name(),
                "type", "access",
                "employeeId", employee.getId(),
                "departmentId", employee.getDepartment() == null ? null : employee.getDepartment().getId(),
                "fullName", employee.getFirstName() + " " + employee.getLastName()
        ));
    }

    public String createRefreshToken(Employee employee) {
        return createToken(employee, refreshTokenDays * 24 * 60 * 60, Map.of("type", "refresh"));
    }

    public String subject(String token) {
        return claims(token).getSubject();
    }

    public String tokenType(String token) {
        return claims(token).get("type", String.class);
    }

    public boolean isValid(String token) {
        claims(token);
        return true;
    }

    private String createToken(Employee employee, long seconds, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(employee.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(key)
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
