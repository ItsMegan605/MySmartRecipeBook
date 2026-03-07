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

    //Bean che crea il filtro JWT, filtro intercetta ogni richiesta HTTP e verifica la validità del token
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    //utilizza UserDetailsService per recuperare l'utente e PasswordEncoder per verificare la password
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    //configurazione regole di sicurezza e di permessi
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http    //disabilito CSRF perché l'applicazione utilizza JWT (stateless)
                .csrf(csrf -> csrf.disable())

                //nessuna sessione lato server: l'autenticazione è gestita tramite token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // definizione delle autorizzazioni sugli endpoint
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
                //registrazione del provider di autenticazione
                .authenticationProvider(authenticationProvider());

         /* Inserimento del filtro JWT nella catena dei filtri di Spring Security.
         * Il filtro viene eseguito prima del filtro standard di autenticazione
         * UsernamePasswordAuthenticationFilter. */
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
     /* Password encoder utilizzato per cifrare le password nel database.
     * BCrypt è uno degli algoritmi più sicuri per l'hashing delle password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

     /* Bean che espone l'AuthenticationManager, utilizzato nel processo di login
     per autenticare l'utente */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
