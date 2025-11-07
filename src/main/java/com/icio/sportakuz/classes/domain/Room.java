package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Reprezentuje salę/miejsce, w której odbywają się zajęcia.
 * Zawiera podstawowe informacje o nazwie, lokalizacji, pojemności oraz aktywności.
 * Kolumna created_at jest wypełniana przez bazę (trigger / default NOW()).
 */
@Entity
@Table(name = "rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_rooms_name", columnNames = "name"))
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
