package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.ClassStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Pojedyncze wystąpienie konkretnych zajęć (np. poniedziałek 10:00).
 * Może być powiązane z serią (ClassSeries) lub istnieć samodzielnie (gdy series == null).
 * Zawiera bieżący status (ClassStatus) oraz parametry realizacyjne.
 */
@Entity
@Table(name = "classes")
@Getter
@Setter
public class ClassOccurrence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opcjonalna seria, z której to wystąpienie pochodzi. */
    @ManyToOne @JoinColumn(name="series_id")
    private ClassSeries series;

    /** Typ zajęć. */
    @ManyToOne(optional=false) @JoinColumn(name="type_id", nullable=false)
    private ClassType type;

    /** Instruktor prowadzący to wystąpienie. */
    @ManyToOne(optional=false) @JoinColumn(name="instructor_id", nullable=false)
    private Instructor instructor;

    /** Sala, w której odbywa się wystąpienie. */
    @ManyToOne(optional=false) @JoinColumn(name="room_id", nullable=false)
    private Room room;

    /** Czas rozpoczęcia. */
    @Column(name="start_time", nullable=false)
    private OffsetDateTime startTime;

    /** Czas zakończenia. */
    @Column(name="end_time", nullable=false)
    private OffsetDateTime endTime;

    /** Dostępna pojemność (liczba miejsc). */
    @Column(nullable=false)
    private Integer capacity;

    /** Aktualny status zajęć. */
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private ClassStatus status = ClassStatus.PLANNED;

    /** Notatka organizacyjna. */
    @Column(columnDefinition="text")
    private String note;

    /** Timestamp utworzenia (DB). */
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp ostatniej aktualizacji (DB). */
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    /** Instruktor, za którego prowadzone są zajęcia w zastępstwie (jeśli dotyczy). */
    @ManyToOne
    @JoinColumn(name = "substituted_for_id")
    private Instructor substitutedFor;
}
