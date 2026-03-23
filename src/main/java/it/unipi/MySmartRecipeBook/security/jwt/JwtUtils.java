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

/*classe che gestisce tutte le operazioni relative ai JWT.
  In particolare:
    - genera un token quando un utente effettua il login
    - estrae le informazioni dal token
    - verifica la validità del token
 */

@Component
public class JwtUtils {

    // chiave segreta utilizzata per firmare il token
    // viene letta dal file application.properties
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // durata del token (in millisecondi)
    // anche questo valore è definito nel file application.properties
    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    // metodo che genera la chiave crittografica utilizzata
    // per firmare e verificare il token JWT.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    //metodo per generare il token
    public String generateJwtToken(Authentication authentication) {

        // recupero l'utente autenticato
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String id = userPrincipal.getId();
        String username = authentication.getName();

        // estraggo i ruoli dell'utente
        var roles = authentication.getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        //costruzione del token
        return Jwts.builder()
                .setSubject(id)
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }


    public java.util.List<String> getRolesFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", java.util.List.class);
    }

    /* Verifica la validità del token JWT.
     * Il metodo controlla:
     * - che la firma del token sia valida
     * - che il token non sia scaduto
     * - che il formato del token sia corretto
     *
     * Se il token è valido restituisce true,
     * altrimenti restituisce false.
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

/* flusso di autenticazione
Login
   ↓
AuthenticationManager
   ↓
JwtUtils.generateJwtToken()
   ↓
JWT inviato al client
   ↓
Client lo manda in ogni richiesta (Authorization: Bearer ...)
   ↓
AuthTokenFilter verifica il token
   ↓
utente autenticato

 */