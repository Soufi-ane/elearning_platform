package com.elearn.api.service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  public String generateToken(String username, String userId, Role role){
    long expirationTime = 1000 * 60 * 60 * 24;
    Instant now = Instant.now();
    Instant expirationDate = now.plusMillis(expirationTime);

    return Jwts.builder()
      .setSubject(username)
      .claim("id", userId)
      .claim("role", role.name())
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(expirationDate))
      .signWith(getSigninKey(),SignatureAlgorithm.HS256)
      .compact();
  }

  public String extractUsername(String token){
    return extractClaim(token,Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public Claims extractAllClaims(String token){
    return Jwts.parserBuilder()
      .setSigningKey(getSigninKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  private Key getSigninKey(){
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public boolean isTokenValid(String token, UserDetails userDetails){
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token){
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

}

