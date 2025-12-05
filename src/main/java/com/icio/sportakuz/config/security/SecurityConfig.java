package com.icio.sportakuz.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Wstrzykujemy nasz serwis (ten z Kroku 1)
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Publiczne zasoby (CSS, JS, Obrazki) + Login + Rejestracja (jeśli będzie)
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/register").permitAll()
                        // Panel Admina
                        .requestMatchers("/panel/admin/**").hasRole("ADMIN")
                        // Reszta wymaga zalogowania
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Twój endpoint w LoginController
                        .loginProcessingUrl("/login") // Gdzie POSTuje formularz z HTMLa
                        .defaultSuccessUrl("/panel/admin", true) // Gdzie przekierować po sukcesie
                        .usernameParameter("username") // Tak masz w login.html: name="username"
                        .passwordParameter("password") // Tak masz w login.html: name="password"
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // Ten Bean łączy Springa z Twoją bazą danych
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}