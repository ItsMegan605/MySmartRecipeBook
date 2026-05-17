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

import java.io.IOException;
import java.util.List;

/**
 * This filter is executed for every HTTP request in the application.
 * Its responsibilities are:
 * 1. Extract the JWT token from the Authorization header
 * 2. Validate the token
 * 3. Extract user information from the token
 * 4. Set the authenticated user into Spring Security's SecurityContext
 * It extends OncePerRequestFilter to guarantee that the filter
 * is executed only once per request.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Core filtering logic executed for each request.
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the chain of filters to continue execution
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        //retrieve the Authorization header from the HTTP request
        String headerAuth = request.getHeader("Authorization");

        //check if the header exists and contains a Bearer token
        //standard format: Authorization: Bearer <token>
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {

            //extract the token by removing "Bearer "
            String jwt = headerAuth.substring(7);

            //validate the token
            if (jwtUtils.validateJwtToken(jwt)) {

                //extract user information from the token
                String id = jwtUtils.getIdFromJwtToken(jwt);
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                //convert roles into GrantedAuthority objects
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                //create a UserPrincipal WITHOUT querying the database
                //(all necessary information is already inside the JWT)
                UserPrincipal userPrincipal = new UserPrincipal(
                        id,
                        username,
                        null,
                        authorities
                );

                //create an Authentication object representing the authenticated user
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities
                        );

                // attach request details (e.g., IP, session info)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                //store the authentication inside the SecurityContext.From this point on, Spring Security considers
                // the request as authenticated

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        //continue the filter chain
        filterChain.doFilter(request, response);
    }
}