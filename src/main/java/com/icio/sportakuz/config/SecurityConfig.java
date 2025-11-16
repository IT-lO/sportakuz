package com.icio.sportakuz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Definiuje, kto ma dostęp do czego (reguły dostępu).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/panel/admin/**").hasRole("ADMIN")
                        .requestMatchers("/panel/user/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/panel/admin", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Szyfrowanie haseł.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Zasilanie Spring Security w użytkowników.
     * * !!! UWAGA: TO JEST TYMCZASOWE - NA POTRZEBY TESTÓW !!!
     * * Docelowo, ten Bean powinien zostać zastąpiony przez implementację UserDetailsService,
     * która pobiera użytkowników z Twojej bazy danych (z repozytorium użytkowników).
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        String encodedPassword = passwordEncoder.encode("password");

        UserDetails admin = User.builder()
                .username("admin")
                .password(encodedPassword)
                .roles("ADMIN", "USER")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(encodedPassword)
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}