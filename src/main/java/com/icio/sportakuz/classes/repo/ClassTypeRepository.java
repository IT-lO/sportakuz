package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium typów zajęć {@link ClassType} z metodą wyszukiwania po nazwie.
 */
public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {
    /** Wyszukuje typ zajęć po jego nazwie. */
    Optional<ClassType> findByName(String name);
}
