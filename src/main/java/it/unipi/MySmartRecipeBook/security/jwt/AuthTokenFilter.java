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

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {

            String jwt = headerAuth.substring(7);

            if (jwtUtils.validateJwtToken(jwt)) {

                String id = jwtUtils.getIdFromJwtToken(jwt);
                String name = jwtUtils.getNameFromJwtToken(jwt);
                String surname = jwtUtils.getSurnameFromJwtToken(jwt);

                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Creo un UserPrincipal SENZA query al DB
                UserPrincipal userPrincipal = new UserPrincipal(
                        id,
                        name,
                        surname,
                        null, // password non serve nelle request
                        authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
