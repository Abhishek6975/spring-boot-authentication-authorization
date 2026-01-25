package com.koyta.auth.services.impl;

import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.JwtTokenExpiredException;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;

    @Getter
    private final long refreshTtlSeconds;
    private final String issuer;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public JwtServiceImpl(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds:3600}") long accessTtlSeconds,         // 60 minutes
            @Value("${security.jwt.refresh-ttl-seconds:1209600}") long refreshTtlSeconds,   // 14 days
            @Value("${security.jwt.issuer:auth-backend}") String issuer,
            UserRepository userRepository,
            RoleRepository roleRepository) {

        if (secret == null || secret.length() < 64) {
            throw new IllegalStateException("JWT secret must be at least 64 characters. Provide via env JWT_SECRET.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public String generateAccessToken(User user, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                     //   "email", user.getEmail(),
                            "id",  user.getId(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(key)
                .compact();
    }



    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return	Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {

            throw new JwtTokenExpiredException("Token is Expired");
        } catch (JwtException e) {

            throw new JwtTokenExpiredException("invalid Jwt token");
        } catch (Exception e) {

            throw e;
        }
    }

    public boolean isAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        return "access".equals(claims.get("typ"));
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return "refresh".equals(claims.get("typ"));
    }

    public String getUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public String getJti(String token) {
        return extractAllClaims(token).getId();
    }

    @Override
    public String extractUserName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        String userName = extractUserName(token);
        return userName.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private Boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        Date expirationDate = claims.getExpiration();

        // 27 Dec(today) - 28 Dec(Expir)
        return expirationDate.before(new Date());
    }


}
