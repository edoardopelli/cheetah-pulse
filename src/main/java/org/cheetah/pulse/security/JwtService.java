// src/main/java/com/setupnet/johndoe/security/JwtService.java
package org.cheetah.pulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

  @Value("${app.security.jwt.secret}") private String secret;
  @Value("${app.security.jwt.expiration-seconds:3600}") private long exp;
  @Value("${app.security.jwt.reset-expiration-seconds:600}") private long resetExp; // 10 minuti
  @Value("${app.security.jwt.issuer:auth-service}") private String issuer;

  private SecretKey signingKey;

  @PostConstruct
  void initKey() {
    byte[] bytes;
    // Se sembra Base64, decodifica; altrimenti usa UTF-8
    if (secret.matches("^[A-Za-z0-9+/=\\r\\n]+$") && !secret.contains(":")) {
      bytes = Decoders.BASE64.decode(secret.replace("\n","").replace("\r",""));
    } else {
      bytes = secret.getBytes(StandardCharsets.UTF_8);
    }
    if (bytes.length < 32) { // 256 bit min per HS256
      throw new IllegalStateException("JWT secret too short: need >= 32 bytes (256 bit).");
    }
    this.signingKey = Keys.hmacShaKeyFor(bytes);
  }

  public String generate(String subject) {
    return Jwts.builder()
        .setSubject(subject)
        .setIssuer(issuer)
        .setIssuedAt(new Date())
        .setExpiration(Date.from(Instant.now().plusSeconds(exp)))
        .signWith(signingKey)
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .requireIssuer(issuer)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /** Token breve per reset password */
  public String generateResetToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuer(issuer)
        .claim("typ", "pwd_reset")
        .setIssuedAt(new Date())
        .setExpiration(Date.from(Instant.now().plusSeconds(resetExp)))
        .signWith(signingKey)
        .compact();
  }

  public String parseAndValidateResetToken(String token) {
    var jws = Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .requireIssuer(issuer)
        .build()
        .parseClaimsJws(token);
    var claims = jws.getBody();
    if (!"pwd_reset".equals(claims.get("typ"))) {
      throw new IllegalArgumentException("Invalid reset token type");
    }
    return claims.getSubject();
  }
}