package com.icio.sportakuz.config;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ten kod wykona się automatycznie po starcie aplikacji na Tomcacie
        createAdminIfNotFound();
        createUserIfNotFound();
    }

    private void createAdminIfNotFound() {
        if (!userRepository.existsByEmail("admin@sportakuz.pl")) {
            User admin = new User();
            admin.setEmail("admin@sportakuz.pl");
            admin.setFirstName("Główny");
            admin.setLastName("Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println(">>> DATA_INIT: Utworzono ADMINA");
        }
    }

    private void createUserIfNotFound() {
        if (!userRepository.existsByEmail("user@sportakuz.pl")) {
            User user = new User();
            user.setEmail("user@sportakuz.pl");
            user.setFirstName("Jan");
            user.setLastName("Świątek");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(UserRole.ROLE_USER);
            user.setActive(true);
            userRepository.save(user);
            System.out.println(">>> DATA_INIT: Utworzono USERA");
        }
        if (!userRepository.existsByEmail("user1@sportakuz.pl")) {
            User user = new User();
            user.setEmail("user1@sportakuz.pl");
            user.setFirstName("Barabasz");
            user.setLastName("Jugosławski");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(UserRole.ROLE_USER);
            user.setActive(true);
            userRepository.save(user);
            System.out.println(">>> DATA_INIT: Utworzono USERA");
        }
    }
}