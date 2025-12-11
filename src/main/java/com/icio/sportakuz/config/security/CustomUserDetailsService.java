package com.icio.sportakuz.config.security; // lub .service, zależy jak masz strukturę

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Szukamy w bazie po emailu (bo w login.html użytkownik wpisuje email/login)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + email));

        // 2. Konwertujemy Twojego Usera na Usera Springowego
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // To musi być hash!
                user.isActive(), // Czy konto aktywne
                true, true, true, // Konta nie wygasłe/zablokowane (można rozbudować)
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())) // Np. ROLE_ADMIN
        );
    }
}