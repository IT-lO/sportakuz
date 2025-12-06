package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.ClassStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Pojedyncze wystąpienie konkretnych zajęć.
 * Logika oparta o Czas Startu + Czas Trwania (minuty).
 * Czas Zakończenia (endTime) jest wyliczany automatycznie.
 */
@Entity
@Table(name = "activities")
@Getter
@Setter
public class Activity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name="series_id")
    private ActivitySeries series;

    @ManyToOne(optional=false) @JoinColumn(name="type_id", nullable=false)
    private ActivityType type;

    @ManyToOne(optional=false)
    @JoinColumn(name="instructor_id", nullable=false)
    private User instructor;

    @ManyToOne(optional=false) @JoinColumn(name="room_id", nullable=false)
    private Room room;

    // --- ZMIANA: Logika czasu ---

    /** Czas rozpoczęcia. */
    @Column(name="start_time", nullable=false)
    private OffsetDateTime startTime;

    /** Czas trwania w minutach (wymagane pole w bazie). */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** * Czas zakończenia.
     * Jest wyliczany automatycznie przed zapisem na podstawie startu i trwania.
     * Nie musisz go ustawiać ręcznie w kontrolerze.
     */
    @Column(name="end_time", nullable=false)
    private OffsetDateTime endTime;

    // -----------------------------

    @Column(nullable=false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private ClassStatus status = ClassStatus.PLANNED;

    @Column(columnDefinition="text")
    private String note;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "substituted_for_id")
    private User substitutedFor;

    /**
     * Magiczna metoda, która uruchamia się automatycznie PRZED zapisem (INSERT)
     * lub aktualizacją (UPDATE) w bazie danych.
     * Wylicza endTime, żeby baza danych nie krzyczała o nulle.
     */
    @PrePersist
    @PreUpdate
    public void calculateEndTime() {
        if (startTime != null && durationMinutes != null) {
            this.endTime = startTime.plusMinutes(durationMinutes);
        }
    }
}