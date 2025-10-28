package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {
    Optional<ClassType> findByName(String name);
}
