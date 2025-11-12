package com.icio.sportakuz.classes.domain;

import com.icio.sportakuz.classtypes.DifficultyLevel;
import jakarta.persistence.*;

/**
 * Definicja typu zajęć (np. Joga, Pilates). Zawiera nazwę, opis, domyślny czas trwania
 * oraz trudność (enum). Używane przez ClassSeries i ClassOccurrence.
 */
@Entity
@Table(name = "class_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_class_types_name", columnNames = "name"))
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }
}
