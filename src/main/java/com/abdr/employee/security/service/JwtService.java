package com.abdr.employee.security.service;

import com.abdr.employee.utils.JwtAndSignatureHandler;
import com.abdr.employee.utils.SecretKeyGeneratingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    HashMap<String, Object> claims = new HashMap<>();
    private String secretKey;

    public JwtService() throws SecretKeyGeneratingException {
        secretKey = generateSecretKey();
    }

    private String generateSecretKey() throws SecretKeyGeneratingException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGenerator.generateKey();
            System.out.println("secret key : " + secretKey.toString());
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new SecretKeyGeneratingException("Error generating secret key : " + e);
        }
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +2 * 60 * 60 * 1000))  //2 hrs
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature for token: {}", token);
            throw new JwtAndSignatureHandler("Invalid JWT signature: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token has expired: {}", token);
            throw new JwtAndSignatureHandler("JWT token has expired: " + ex.getMessage());
        } catch (JwtException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            throw new JwtAndSignatureHandler("JWT validation failed: " + ex.getMessage());
        }

    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
