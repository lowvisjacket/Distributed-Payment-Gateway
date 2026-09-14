package com.example.payment_processor.Security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;
    public String generateToken(UUID customerId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, customerId);
    }

    private String createToken(Map<String, Object> claims, UUID customerId) {
        return Jwts.builder()
                .claims(claims)
                .subject(customerId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public UUID extractCustomerId(String token) {
        return UUID.fromString(extractClaims(token, Claims::getSubject));
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UUID customerId, UserDetails userDetails) {
        final UUID tokenCustomerId = extractCustomerId(token);
        return (userDetails.isEnabled()
                && tokenCustomerId.equals(customerId)
                && !isTokenExpired(token));
    }
}
