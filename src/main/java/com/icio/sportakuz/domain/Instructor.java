package com.icio.sportakuz.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "instructors",
        uniqueConstraints = @UniqueConstraint(name = "uk_instructors_email", columnNames = "email"))
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name", nullable=false, length=100)
    private String firstName;

    @Column(name="last_name", nullable=false, length=100)
    private String lastName;

    @Column(nullable=false, length=255)
    private String email;

    @Column(length=40)
    private String phone;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(nullable=false)
    private boolean active = true;

    // DB ustawia NOW(); trzymamy tylko odczyt
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    // gettery/settery/konstruktory bez- i pełnoargumentowe (lombok opcjonalnie)
}
