package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Rezerwacja miejsca uczestnika na konkretnym wystąpieniu zajęć (ClassOccurrence).
 * Zawiera dane identyfikujące użytkownika (userName) oraz bieżący status.
 * Pola created_at / cancelled_at są zarządzane przez bazę lub logikę serwera.
 */
@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name="idx_bookings_class", columnList="class_id"),
                @Index(name="idx_bookings_status", columnList="status")
        })
@Getter
@Setter
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Wystąpienie zajęć, którego dotyczy rezerwacja. */
    @ManyToOne(optional=false)
    @JoinColumn(name="class_id", nullable=false)
    private ClassOccurrence clazz;

    /** Nazwa / identyfikator użytkownika dokonującego rezerwacji. */
    @Column(name="user_name", nullable=false, length=100)
    private String userName;

    /** Aktualny status rezerwacji. */
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private BookingStatus status;

    /** Timestamp utworzenia (DB). */
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp anulowania (jeśli status CANCELLED). */
    @Column(name="cancelled_at")
    private OffsetDateTime cancelledAt;
}
