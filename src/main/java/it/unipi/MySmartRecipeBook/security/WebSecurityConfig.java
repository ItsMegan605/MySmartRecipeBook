package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.security.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security configuration class.
 * Uses stateless authentication based on JWT.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;


    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Creates the JWT authentication filter.
     * This filter intercepts every HTTP request
     * and validates the JWT token.
     *
     * @return AuthTokenFilter instance
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configures the authentication provider.
     *
     * Uses:
     * - UserDetailsService to load user data
     * - PasswordEncoder to verify passwords
     *
     * @return DaoAuthenticationProvider instance
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Configures security rules and filter chain.
     *
     * @param http HttpSecurity object used to configure security
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //disable CSRF since the application uses JWT (stateless)
                .csrf(csrf -> csrf.disable())

                //no server-side session: authentication is token-based
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //define endpoint authorization rules
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // SWAGGER endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // CHEF - recipe management
                        .requestMatchers(HttpMethod.POST, "/api/recipes/**").hasRole("CHEF")
                        .requestMatchers(HttpMethod.PUT, "/api/recipes/**").hasRole("CHEF")
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").hasRole("CHEF")

                        // CHEF - profile management
                        .requestMatchers("/api/chefs/**").hasRole("CHEF")

                        // FOODIE - profile management
                        .requestMatchers("/api/foodies/**").hasRole("FOODIE")

                        // FOODIE - saved recipes
                        .requestMatchers("/api/savedrecipes/**").hasRole("FOODIE")

                        // FOODIE - SmartFridge
                        .requestMatchers("/api/fridge/**").hasRole("FOODIE")

                        // FOODIE - SmartShoppingList
                        .requestMatchers("/api/shopping/**").hasRole("FOODIE")

                        // ADMIN endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                //register authentication provider
                .authenticationProvider(authenticationProvider());

        /*
         * Add the JWT filter to the Spring Security filter chain.
         * It is executed before the default UsernamePasswordAuthenticationFilter.
         */
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder used to hash passwords.
     * BCrypt is a secure hashing algorithm widely used for password storage.
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean.
     * Used during login to authenticate users.
     *
     * @param authConfig authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}