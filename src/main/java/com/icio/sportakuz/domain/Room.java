package com.icio.sportakuz.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_rooms_name", columnNames = "name"))
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(length=255)
    private String location;

    @Column(nullable=false)
    private Integer capacity; // CHECK (capacity > 0) – walidacja po stronie DB

    @Column(nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    // gettery/settery/konstruktory
}
