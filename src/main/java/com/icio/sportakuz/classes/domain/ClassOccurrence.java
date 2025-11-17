package com.icio.sportakuz.classes.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Pojedyncze wystąpienie konkretnych zajęć (np. poniedziałek 10:00).
 * Może być powiązane z serią (ClassSeries) lub istnieć samodzielnie (gdy series == null).
 * Zawiera bieżący status (ClassStatus) oraz parametry realizacyjne.
 */
@Entity
@Table(name = "classes")
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
    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    /** Timestamp ostatniej aktualizacji (DB). */
    @Column(name="updated_at", nullable=false, insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    /** Instruktor, za którego prowadzone są zajęcia w zastępstwie (jeśli dotyczy). */
    @ManyToOne
    @JoinColumn(name = "substituted_for_id")
    private Instructor substitutedFor;

    public Long getId() {
        return id;
    }

    public ClassSeries getSeries() {
        return series;
    }

    public void setSeries(ClassSeries series) {
        this.series = series;
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

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instructor getSubstitutedFor() {
        return substitutedFor;
    }

    public void setSubstitutedFor(Instructor substitutedFor) {
        this.substitutedFor = substitutedFor;
    }
}
