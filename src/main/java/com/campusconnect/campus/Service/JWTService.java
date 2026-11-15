package com.campusconnect.campus.Service;

import com.campusconnect.campus.Configuration.JwtRequestFilter;
import com.campusconnect.campus.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JWTService {


  @Value("${jwt.secret}")
  private String secretKey;

    private Key getSecretKeyObject(){
        // byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        byte[] keyBytes= secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
    // GENERATE ACCESS TOKEN
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();// Agar extra data dalna ho
        claims.put("username",user.getUsername());
        claims.put("role",user.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail()) // subject {emailID} set krte hain isse hm
                .issuedAt(new Date(System.currentTimeMillis())) // iat set krte hain isse hm
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 15)) // expiration time set krte
                .signWith(getSecretKeyObject()) // calls the function
                .compact();
    }


    public String extractEmails(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // ABOVE METHOD IS PUBLIC SO THAT IT GET CALLED BY OUTSIDE CLASS -->WHICH IS jwtRequestFilter
    // IN  the above method is all about extracting  email which is stored in string and  we have to
    // extract it from User

    // WHY THIS METHOD IS PUBLIC ??
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Poora Payload extract kr lete hain
        return claimsResolver.apply(claims);
    }
    // above one is generic claim extractor function

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSecretKeyObject()) // setSigningKey() ki jagah verifyWith()
                .build()
                .parseSignedClaims(token)    // parseClaimsJws() ki jagah parseSignedClaims()
                .getPayload();               // getBody() ki jagah getPayload()
    }
    //

    // now methods -->return below to see weather the token is valid or not
    public boolean tokenIsValid(String token, User user){
        String email=extractEmails(token);
        return (Objects.equals(email, user.getEmail()) && !isTokenExpired(token));
        // int this two things are checks -->one is user email is equal to the email we extract from token
        // second thing is ---> agr token expired nahi and toh its good
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration); // poora claims ko return kr do ismein
    }

}
