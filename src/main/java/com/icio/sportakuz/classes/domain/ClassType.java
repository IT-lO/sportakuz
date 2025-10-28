package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "class_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_class_types_name", columnNames = "name"))
public class ClassType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(columnDefinition="text")
    private String description;

    @Column(name="default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(length=20) // w DB brak enumu/constraintu
    private String difficulty;

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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
