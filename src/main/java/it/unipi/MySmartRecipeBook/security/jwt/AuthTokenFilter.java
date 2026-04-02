package it.unipi.MySmartRecipeBook.security.jwt;

import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import it.unipi.MySmartRecipeBook.security.jwt.JwtUtils;

import java.io.IOException;
import java.util.List;

/**
 * Questo filtro viene eseguito per ogni richiesta HTTP dell'applicazione.
 *  * Il suo compito è:
 *  * 1. leggere il token JWT dall'header Authorization
 *  * 2. verificarne la validità
 *  * 3. estrarre le informazioni dell'utente
 *  * 4. impostare l'utente autenticato nel SecurityContext di Spring
 *  *
 *  * Estende OncePerRequestFilter per garantire che il filtro venga
 *  * eseguito una sola volta per ogni richiesta.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     *
     * @param request parametro che prende la richiesta
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     * @see JwtUtils#
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // recupero l'header Authorization dalla richiesta HTTP
        String headerAuth = request.getHeader("Authorization");

        // Controllo se l'header esiste e se contiene un token Bearer,
        //Il formato standard del JWT nell'header è:  Authorization: Bearer <token>
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {

            // estraggo il token rimuovendo la parte "Bearer "
            String jwt = headerAuth.substring(7);

            // verifico se il token è valido
            if (jwtUtils.validateJwtToken(jwt)) {

                // estraggo le informazioni salvate nel token
                String id = jwtUtils.getIdFromJwtToken(jwt);
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                // convertiamo i ruoli in GrantedAuthority
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Creo un UserPrincipal SENZA query al DB (ho gia tutte le info dentro JWT)
                UserPrincipal userPrincipal = new UserPrincipal(
                        id,
                        username,
                        null,
                        authorities
                );
                // Creo un oggetto Authentication che rappresenta l'utente autenticato nel sistema Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities
                        );
                // aggiungo i dettagli della richiesta HTTP
                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                /*
                 * Salviamo l'autenticazione nel SecurityContext.
                 * Da questo momento in poi Spring Security considera
                 * la richiesta come autenticata.
                 */
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        // continuo la catena dei filtri per far continuare la richiesta
        filterChain.doFilter(request, response);
    }
}
