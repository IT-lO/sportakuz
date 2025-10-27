package com.icio.sportakuz.repository;

import com.icio.sportakuz.domain.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByEmail(String email);
}
