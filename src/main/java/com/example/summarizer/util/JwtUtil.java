package com.example.summarizer.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

  private static final String SECRET = "super-secret-key-change-this-super-secret-key-change-this";

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String username) {
    return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
            .signWith(getSigningKey())
            .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }

  public boolean isTokenValid(String token) {
    try {
      extractUsername(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}