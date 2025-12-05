package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.User;
import com.icio.sportakuz.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository<User, Long> oznacza:
// "Jestem magazynierem od obiektów typu User, a ich dowodem tożsamości (ID) jest liczba (Long)"
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatycznie wygeneruje zapytanie SQL na podstawie nazwy tej metody!
    Optional<User> findByEmail(String email);
    // Do list rozwijanych (np. przy tworzeniu zajęć)
    List<User> findByRole(UserRole role);

    // Do statystyk na stronie głównej (liczba instruktorów)
    long countByRole(UserRole role);

    // Możemy dodać też sprawdzanie czy email już istnieje (np. przy rejestracji)
    boolean existsByEmail(String email);


}