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

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // PUBBLICO
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // CHEF - gestione ricette
                        .requestMatchers(HttpMethod.POST, "/api/recipes/**").hasRole("CHEF")
                        .requestMatchers(HttpMethod.PUT, "/api/recipes/**").hasRole("CHEF")
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").hasRole("CHEF")

                        //CHEF - gestione proprio profilo
                        .requestMatchers("/api/chefs/**").hasRole("CHEF")

                        //FOODIE - gestione profilo
                        .requestMatchers("/api/foodies/**").hasRole("FOODIE")

                        // FOODIE - salvataggio ricette
                        .requestMatchers("/api/savedrecipes/**").hasRole("FOODIE")

                        //FOODIE - SmartFridge
                        .requestMatchers("/api/fridge/**").hasRole("FOODIE")

                        //FOODIE - SmartShoppingList
                        .requestMatchers("/api/shopping/**").hasRole("FOODIE")

                        //aggiungere endpoint amministratore
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // tutto il resto autenticato
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
