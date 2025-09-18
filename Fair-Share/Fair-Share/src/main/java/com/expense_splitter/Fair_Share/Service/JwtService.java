package com.expense_splitter.Fair_Share.Service;

import com.expense_splitter.Fair_Share.Entity.users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private String secretKey = null;
    public String GenerateToken(users user) {
        Map<String,Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuer("Kunj")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .and()
                .signWith(GenerateKey(),io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

    }

    public String  getSecretKey(){
        return secretKey = "HFpJ/rc58K3TXzm6G0V7EzcD0sc2v9MHuebpKGT8/Fw=";
    }

    private SecretKey GenerateKey() {

        byte[] decode = Decoders.BASE64.decode(getSecretKey());
        return Keys.hmacShaKeyFor(decode);
    }


    public String ExtractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimresolver) {
        Claims  claims = extractClaims(token);
        return claimresolver.apply(claims);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(GenerateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        final String username = ExtractUsername(jwt);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    private Date extractExpiration(String jwt) {
        return extractClaims(jwt,Claims::getExpiration);
    }

    public String generateToken(String email) {
        Map<String,Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuer("Kunj")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .and()
                .signWith(GenerateKey(),io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
