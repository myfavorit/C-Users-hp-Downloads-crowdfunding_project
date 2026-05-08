package com.ensias.crowdfunding_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. EXTRACTION

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // ← Méthode pour jjwt 0.12.x
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ============================================================
    // 2. GÉNÉRATION
    // ============================================================

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)                    // ← setClaims() devient claims()
                .subject(userDetails.getUsername()) // ← setSubject() devient subject()
                .issuedAt(new Date())              // ← setIssuedAt() devient issuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration)) // ← setExpiration() devient expiration()
                .signWith(getSigningKey(), Jwts.SIG.HS256)  // ← nouvelle syntaxe
                .compact();
    }

    // ============================================================
    // 3. VALIDATION
    // ============================================================

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token invalide: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenStructureValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expiré");
            return false;
        } catch (MalformedJwtException | SignatureException e) {
            log.debug("Token invalide: {}", e.getClass().getSimpleName());
            return false;
        } catch (IllegalArgumentException e) {
            log.debug("Token null");
            return false;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la validation du token", e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ============================================================
    // 4. UTILITAIRES
    // ============================================================

    public long getTimeRemaining(String token) {
        Date expiration = extractExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public boolean isAboutToExpire(String token) {
        return getTimeRemaining(token) < 300000; // 5 minutes
    }
}