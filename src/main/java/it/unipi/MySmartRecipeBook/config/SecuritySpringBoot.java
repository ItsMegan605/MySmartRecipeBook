/* package it.unipi.MySmartRecipeBook.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecuritySpringBoot {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Sblocchiamo il login e la registrazione (se la aggiungerai)
                        .requestMatchers("/api/chef/login").permitAll()
                        .requestMatchers("/api/chef/register").permitAll() // Opzionale

                        // 2. Tutto il resto richiede autenticazione
                        .anyRequest().authenticated()
                )
                // Se usi un login personalizzato via Controller,
                // potresti non aver bisogno di httpBasic o FormLogin qui.
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


 */
