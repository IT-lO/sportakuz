package com.icio.sportakuz.domain;

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

    // get/set
}
