package it.unipi.MySmartRecipeBook.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

/**
 * Utility class that handles all JWT-related operations.

 */
@Component
public class JwtUtils {

    //secret key used to sign the JWT (loaded from application.properties)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    //token expiration time in milliseconds (loaded from application.properties)
    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    /**
     * Generates the cryptographic key used to sign and validate the JWT.
     * @return the signing key
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token from the authenticated user.
     * @param authentication the authenticated user object provided by Spring Security
     * @return the generated JWT token
     */
    public String generateJwtToken(Authentication authentication) {

        //retrieve the authenticated user
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String id = userPrincipal.getId();
        String username = authentication.getName();

        //extract user roles
        var roles = authentication.getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        //build the JWT token
        return Jwts.builder()
                .setSubject(id)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the user ID (subject) from the JWT.
     * @param token the JWT token
     * @return the user ID
     */
    public String getIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extracts the username from the JWT.
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }

    /**
     * Extracts user roles from the JWT.
     * @param token the JWT token
     * @return the list of roles
     */
    public java.util.List<String> getRolesFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", java.util.List.class);
    }

    /**
     * Validates the JWT token.
     * It checks:
     * - token signature validity
     * - token expiration
     * - correct token format
     *
     * @param authToken the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}

