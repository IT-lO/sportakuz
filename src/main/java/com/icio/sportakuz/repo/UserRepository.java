package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<User, Long> oznacza:
// "Jestem magazynierem od obiektów typu User, a ich dowodem tożsamości (ID) jest liczba (Long)"
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatycznie wygeneruje zapytanie SQL na podstawie nazwy tej metody!
    Optional<User> findByEmail(String email);

    // Możemy dodać też sprawdzanie czy email już istnieje (np. przy rejestracji)
    boolean existsByEmail(String email);
}