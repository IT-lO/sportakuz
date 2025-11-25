package com.icio.sportakuz.repo;

import com.icio.sportakuz.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium dla encji {@link Instructor} z metodą wyszukiwania po e-mailu.
 */
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    /** Wyszukuje instruktora po adresie e-mail (unikalnym). */
    Optional<Instructor> findByEmail(String email);
}
