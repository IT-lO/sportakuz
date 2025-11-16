package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Reprezentuje salę/miejsce, w której odbywają się zajęcia.
 * Zawiera podstawowe informacje o nazwie, lokalizacji, pojemności oraz aktywności.
 * Kolumna created_at jest wypełniana przez bazę (trigger / default NOW()).
 */
@Entity
@Table(name = "rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_rooms_name", columnNames = "name"))
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nazwa sali widoczna dla użytkowników (unikalna). */
    @Column(nullable=false, length=120)
    private String name;

    /** Dodatkowa informacja o lokalizacji (np. piętro, budynek). */
    @Column(length=255)
    private String location;

    /** Maksymalna liczba uczestników; musi być > 0. */
    @Column(nullable=false)
    private Integer capacity; // CHECK (capacity > 0) – walidacja po stronie DB

    /** Czy sala jest aktywna (może być używana do nowych zajęć). */
    @Column(nullable=false)
    private boolean active = true;

    /** Timestamp utworzenia rekordu (ustawiany przez DB). */
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;
}
