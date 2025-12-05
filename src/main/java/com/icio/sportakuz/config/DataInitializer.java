//TODO: Remove this class after testing login OR leave it for easy access without account creation
package com.icio.sportakuz.config;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import com.icio.sportakuz.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Sprawdź czy baza jest pusta (czy istnieje admin)
        if (!userRepository.existsByEmail("admin@sportakuz.pl")) {

            User admin = new User();
            admin.setEmail("admin@sportakuz.pl");
            admin.setFirstName("Główny");
            admin.setLastName("Administrator");
            // Hasło musi być zakodowane! (tutaj: "admin123")
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println(">>> UTWORZONO UŻYTKOWNIKA ADMIN: admin@sportakuz.pl / admin123");
        }
    }
}