package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.DifficultyLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Definicja typu zajęć (np. Joga, Pilates). Zawiera nazwę, opis, domyślny czas trwania
 * oraz trudność (enum). Używane przez ClassSeries i ClassOccurrence.
 */
@Entity
@Table(name = "class_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_class_types_name", columnNames = "name"))
@Getter
@Setter
public class ClassType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nazwa typu zajęć (unikalna). */
    @Column(nullable=false, length=120)
    private String name;

    /** Opis rozszerzony (może zawierać HTML / markdown). */
    @Column(columnDefinition="text")
    private String description;

    /** Domyślny czas trwania w minutach. */
    @Column(name="default_duration_minutes", nullable=false)
    private Integer defaultDurationMinutes;

    /** Poziom trudności jako etykieta (enum). */
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;
}
