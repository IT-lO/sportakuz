package com.icio.sportakuz.repository;

import com.icio.sportakuz.domain.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {
    Optional<ClassType> findByName(String name);
}
