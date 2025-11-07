package com.icio.sportakuz.classes.repo;

import com.icio.sportakuz.classes.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozytorium dostępu do encji {@link Room}.
 * Umożliwia wyszukiwanie sali po unikalnej nazwie.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
    /** Wyszukuje salę po jej nazwie. */
    Optional<Room> findByName(String name);
}
