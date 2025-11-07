package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Reprezentuje serię cyklicznych zajęć opartą o wzorzec powtarzania (RecurrencePattern).
 * Na podstawie tej serii mogą być generowane konkretne wystąpienia (ClassOccurrence).
 * Zawiera ogólne parametry wspólne dla wszystkich wystąpień (instruktor, sala, typ, zakres dat).
 */
@Entity
@Table(name = "class_series")
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
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public ClassType getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public OffsetDateTime getRecurrenceUntil() {
        return recurrenceUntil;
    }

    public void setRecurrenceUntil(OffsetDateTime recurrenceUntil) {
        this.recurrenceUntil = recurrenceUntil;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
