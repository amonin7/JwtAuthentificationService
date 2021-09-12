package com.onlym.config.jwt;

import com.onlym.service.UserDetailsImpl;
import com.onlym.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpirationMs}")
    private long jwtExpiration;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        long expiredAt = new Date().getTime() + jwtExpiration;

        String jwt = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(expiredAt))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        userDetailsService.setUserJwt(userDetails.getUsername(), jwt);
        return jwt;
    }

    public boolean validateJwtToken(String jwt) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(jwt);
            String username = jws.getBody().getSubject();
            String userJwt = userDetailsService.loadUserJwtByUsername(username);
            return userJwt.equals(jwt);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJwtToken(String jwt) {
        return Jwts.parserBuilder().setSigningKey(jwtSecret).build()
                .parseClaimsJws(jwt).getBody().getSubject();
    }

}
