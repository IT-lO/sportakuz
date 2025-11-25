package com.icio.sportakuz.entity;

import com.icio.sportakuz.repo.RecurrencePattern;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Reprezentuje serię cyklicznych zajęć opartą o wzorzec powtarzania (RecurrencePattern).
 * Na podstawie tej serii mogą być generowane konkretne wystąpienia (ClassOccurrence).
 * Zawiera ogólne parametry wspólne dla wszystkich wystąpień (instruktor, sala, typ, zakres dat).
 */
@Entity
@Table(name = "class_series")
@Getter
@Setter
public class ClassSeries {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Typ zajęć dla całej serii. */
    @ManyToOne(optional=false) @JoinColumn(name="type_id", nullable=false)
    private ClassType type;

    /** Instruktor prowadzący serię. */
    @ManyToOne(optional=false) @JoinColumn(name="instructor_id", nullable=false)
    private Instructor instructor;

    /** Domyślna sala dla wystąpień serii. */
    @ManyToOne(optional=false) @JoinColumn(name="room_id", nullable=false)
    private Room room;

    /** Data/godzina rozpoczęcia pierwszego teoretycznego wystąpienia. */
    @Column(name="start_time", nullable=false)
    private OffsetDateTime startTime;

    /** Planowany koniec okna czasowego pojedynczego wystąpienia. */
    @Column(name="end_time", nullable=false)
    private OffsetDateTime endTime;

    /** Domyślna pojemność (może nadpisywać typ). */
    @Column(nullable=false)
    private Integer capacity;

    /** Wzorzec powtarzania serii (dziennie/tygodniowo/miesięcznie). */
    @Enumerated(EnumType.STRING)
    @Column(name="recurrence_pattern", nullable=false, length=20)
    private RecurrencePattern recurrencePattern;

    /** Data graniczna do której generowane są wystąpienia serii. */
    @Column(name="recurrence_until", nullable=false)
    private OffsetDateTime recurrenceUntil;

    /** Notatka organizacyjna (np. wyjątki, uwagi). */
    @Column(columnDefinition="text")
    private String note;

    /** Czy seria jest aktywna (może generować nowe wystąpienia). */
    @Column(nullable=false)
    private boolean active = true;

    /** Timestamp utworzenia rekordu. */
    @CreationTimestamp
    @Column(name="created_at", nullable=false,updatable=false)
    private OffsetDateTime createdAt;
}
