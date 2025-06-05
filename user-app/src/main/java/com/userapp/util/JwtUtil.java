package com.userapp.util;

import com.userapp.dto.JwtDto;
import com.userapp.entity.User;
import com.userapp.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
public class JwtUtil {
    private static final Logger LOGGER = LogManager.getLogger(JwtUtil.class);

    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    private SecretKey key;

    public JwtUtil(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtDto generateToken(Authentication authentication) {
        return generate(authentication);
    }

    private JwtDto generate(Authentication authentication) {
        JwtDto jwtDto = new JwtDto();
        jwtDto.setAccessToken(generateAccessToken(authentication));
        jwtDto.setRefreshToken(generateRefreshToken(authentication));
        return jwtDto;
    }

    public UUID getIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("login", String.class);
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                throw new JwtException("Token is blacklisted");
            }
            parseToken(token);
            return true;
        }
        catch (ExpiredJwtException ex) {
            LOGGER.error("Expired JWT token", ex);
            throw ex;
        }
        catch (UnsupportedJwtException | MalformedJwtException | SecurityException ex) {
            LOGGER.error("Invalid JWT token", ex);
            throw new JwtException("Invalid JWT token", ex);
        }
        catch (Exception ex) {
            LOGGER.error("JWT processing error", ex);
            throw new JwtException("JWT processing error", ex);
        }
    }

    public void blacklistToken(String token) {
        tokenBlacklistService.blacklistToken(token);
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generateAccessToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("login", user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .claims(Map.of("roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())))
                .compact();
    }

    private String generateRefreshToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("login", user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}